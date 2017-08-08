//  Copyright (c) 2014 Couchbase, Inc.
//  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
//  except in compliance with the License. You may obtain a copy of the License at
//    http://www.apache.org/licenses/LICENSE-2.0
//  Unless required by applicable law or agreed to in writing, software distributed under the
//  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
//  either express or implied. See the License for the specific language governing permissions
//  and limitations under the License.

package main

import (
	"flag"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"runtime"

	"github.com/gorilla/mux"

	"github.com/blevesearch/bleve"
	bleveMappingUI "github.com/blevesearch/bleve-mapping-ui"
	bleveHttp "github.com/blevesearch/bleve/http"
	"github.com/blevesearch/bleve/mapping"

	// import general purpose configuration
	_ "github.com/blevesearch/bleve/config"
)

var maxprocs = flag.Int("maxprocs", 4, "GOMAXPROCS")
var bindAddr = flag.String("addr", ":8000", "http listen address")
var dataDir = flag.String("dataDir", "data", "data directory")
var staticEtag = flag.String("staticEtag", "", "optional static etag value.")
var staticPath = flag.String("static", "", "optional path to static directory for web resources")
var staticBleveMappingPath = flag.String("staticBleveMapping", "", "optional path to static-bleve-mapping directory for web resources")
var batchSize = flag.Int("batchSize", 200, "batch size for indexing")
var nShards = flag.Int("shards", 40, "number of indexing shards")

type varLookupFunc func(req *http.Request) string

func main() {
	flag.Parse()

	runtime.GOMAXPROCS(*maxprocs) // set max processors

	// walk the data dir and register index names
	dirEntries, err := ioutil.ReadDir(*dataDir)
	if err != nil {
		log.Fatalf("error reading data dir: %v", err)
	}

	for _, dirInfo := range dirEntries {
		indexPath := *dataDir + string(os.PathSeparator) + dirInfo.Name()

		// skip single files in data dir since a valid index is a directory that
		// contains multiple files
		if !dirInfo.IsDir() {
			log.Printf("not registering %s, skipping", indexPath)
			continue
		}

    i := NewIndexer(indexPath, *nShards, *batchSize, getMapping())
    if err := i.Open(); err != nil {
			log.Printf("error opening index %s: %v", indexPath, err)
		} else {
			log.Printf("registered index: %s", dirInfo.Name())
			RegisterIndexerName(dirInfo.Name(), i)
			// set correct name in stats
			i.alias.SetName(dirInfo.Name())
		}
	}

	router := mux.NewRouter()
	router.StrictSlash(true)

	// default to bindata for static-bleve-mapping resources.
	staticBleveMapping := http.FileServer(bleveMappingUI.AssetFS())
	if *staticBleveMappingPath != "" {
		fi, err := os.Stat(*staticBleveMappingPath)
		if err == nil && fi.IsDir() {
			log.Printf("using static-bleve-mapping resources from %s",
				*staticBleveMappingPath)
			staticBleveMapping = http.FileServer(http.Dir(*staticBleveMappingPath))
		}
	}

	router.PathPrefix("/static-bleve-mapping/").
		Handler(http.StripPrefix("/static-bleve-mapping/", staticBleveMapping))

	// default to bindata for static resources.
	static := http.FileServer(assetFS())
	if *staticPath != "" {
		fi, err := os.Stat(*staticPath)
		if err == nil && fi.IsDir() {
			log.Printf("using static resources from %s",
				*staticPath)
			static = http.FileServer(http.Dir(*staticPath))
		}
	}

	staticFileRouter(router, static)

	// add the API
	bleveMappingUI.RegisterHandlers(router, "/api")

	createIndexHandler := NewCreateIndexHandler(*dataDir, *nShards, *batchSize)
	createIndexHandler.IndexNameLookup = indexNameLookup
	router.Handle("/api/{indexName}", createIndexHandler).Methods("PUT")

	getIndexHandler := NewGetIndexHandler()
	getIndexHandler.IndexNameLookup = indexerNameLookup
	router.Handle("/api/{indexName}", getIndexHandler).Methods("GET")

	deleteIndexHandler := NewDeleteIndexHandler(*dataDir)
	deleteIndexHandler.IndexNameLookup = indexerNameLookup
	router.Handle("/api/{indexName}", deleteIndexHandler).Methods("DELETE")

	listIndexesHandler := NewListIndexesHandler()
	router.Handle("/api", listIndexesHandler).Methods("GET")

	docIndexHandler := NewDocIndexHandler("")
	docIndexHandler.IndexNameLookup = indexerNameLookup
	docIndexHandler.DocIDLookup = docIDLookup
	router.Handle("/api/{indexName}/{docID}", docIndexHandler).Methods("PUT")
	
	bulkDocIndexHandler := NewBulkIndexHandler("")
	bulkDocIndexHandler.IndexNameLookup = indexerNameLookup
	bulkDocIndexHandler.DocIDLookup = docIDLookup
	router.Handle("/_bulk/{indexName}/{docID}", bulkDocIndexHandler).Methods("POST")

	docCountHandler := NewDocCountHandler("")
	docCountHandler.IndexNameLookup = indexerNameLookup
	router.Handle("/api/{indexName}/_count", docCountHandler).Methods("GET")

	docGetHandler := NewDocGetHandler("")
	docGetHandler.IndexNameLookup = indexerNameLookup
	docGetHandler.DocIDLookup = docIDLookup
	router.Handle("/api/{indexName}/{docID}", docGetHandler).Methods("GET")

	docDeleteHandler := NewDocDeleteHandler("")
	docDeleteHandler.IndexNameLookup = indexerNameLookup
	docDeleteHandler.DocIDLookup = docIDLookup
	router.Handle("/api/{indexName}/{docID}", docDeleteHandler).Methods("DELETE")

	searchHandler := NewSearchHandler("")
	searchHandler.IndexNameLookup = indexerNameLookup
	router.Handle("/api/{indexName}/_search", searchHandler).Methods("POST")

	listFieldsHandler := bleveHttp.NewListFieldsHandler("")
	listFieldsHandler.IndexNameLookup = indexerNameLookup
	router.Handle("/api/{indexName}/_fields", listFieldsHandler).Methods("GET")

	debugHandler := bleveHttp.NewDebugDocumentHandler("")
	debugHandler.IndexNameLookup = indexerNameLookup
	debugHandler.DocIDLookup = docIDLookup
	router.Handle("/api/{indexName}/{docID}/_debug", debugHandler).Methods("GET")

	aliasHandler := bleveHttp.NewAliasHandler()
	router.Handle("/api/_aliases", aliasHandler).Methods("POST")

	// start the HTTP server
	http.Handle("/", router)
	log.Printf("Listening on %v", *bindAddr)
	log.Fatal(http.ListenAndServe(*bindAddr, nil))
}

func getMapping() mapping.IndexMapping {
// 	// a generic reusable mapping for english text
// 	standardJustIndexed := bleve.NewTextFieldMapping()
// 	standardJustIndexed.Store = false
// 	standardJustIndexed.IncludeInAll = false
// 	standardJustIndexed.IncludeTermVectors = false
// 	standardJustIndexed.Analyzer = "standard"

// 	articleMapping := bleve.NewDocumentMapping()

// 	// body
// 	articleMapping.AddFieldMappingsAt("Body", standardJustIndexed)

	indexMapping := bleve.NewIndexMapping()
// 	indexMapping.DefaultMapping = articleMapping
// 	indexMapping.DefaultAnalyzer = "standard"
	return indexMapping
}