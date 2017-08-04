package main

import (
	"encoding/json"
	"fmt"
	"flag"
	"time"
	"os"
	"runtime"
	"io/ioutil"
	"net/http"
	bleveHttp "github.com/blevesearch/bleve/http"
)

var batchSize = flag.Int("batchSize", 200, "batch size for indexing")
var nShards = flag.Int("shards", 20, "number of indexing shards")

type BulkIndexHandler struct {
	defaultIndexName string
	IndexNameLookup  varLookupFunc
	DocIDLookup      varLookupFunc
}

func NewBulkIndexHandler(defaultIndexName string) *BulkIndexHandler {
	return &BulkIndexHandler{
		defaultIndexName: defaultIndexName,
	}
}

func (h *BulkIndexHandler) ServeHTTP(w http.ResponseWriter, req *http.Request) {
	
	// find the index to operate on
	var indexName string
	if h.IndexNameLookup != nil {
		indexName = h.IndexNameLookup(req)
	}
	if indexName == "" {
		indexName = h.defaultIndexName
	}
	index := bleveHttp.IndexByName(indexName)
	if index == nil {
		showError(w, req, fmt.Sprintf("no such index '%s'", indexName), 404)
		return
	}
	
	var indexPath = *dataDir + "/" + indexName

  if err := os.RemoveAll(indexPath); err != nil {
		showError(w, req, fmt.Sprintf("failed to remove %s.", indexPath), 404)
		return
	}

  i := NewIndexer(indexPath, *nShards, *batchSize)
  if err := i.Open(); err != nil {
    showError(w, req, fmt.Sprintf("failed to open indexer: ", err), 404)
    return
  }
  
  i.SetName(indexName)
  RegisterIndexName(indexName, i)

	// find the doc id field
	var idField string
	if h.DocIDLookup != nil {
		idField = h.DocIDLookup(req)
	}
	if idField == "" {
		showError(w, req, "document id field cannot be empty", 400)
		return
	}

	// read the request body
	requestBody, err := ioutil.ReadAll(req.Body)
	if err != nil {
		showError(w, req, fmt.Sprintf("error reading request body: %v", err), 400)
		return
	}

	// parse request body as json
	var docs []map[string]interface{}
	err = json.Unmarshal(requestBody, &docs)
	if err != nil {
		showError(w, req, fmt.Sprintf("error parsing request body as JSON: %v", err), 400)
		return
	}

	startTime := time.Now()
	if err := i.Index(idField, docs); err != nil {
		showError(w, req, fmt.Sprintf("failed to index documents: %v", err), 404)
		return
	}
	duration := time.Now().Sub(startTime)

	count, err := i.Count()
	if err != nil {
		showError(w, req, fmt.Sprintf("failed to determine total document count"), 404)
		return
	}
	rate := int(float64(count) / duration.Seconds())

	fmt.Printf("Commencing indexing. GOMAXPROCS: %d, batch size: %d, shards: %d.\n", runtime.GOMAXPROCS(-1), *batchSize, *nShards)

	fmt.Println("Indexing operation took", duration)
	fmt.Printf("%d documents indexed.\n", count)
	fmt.Printf("Indexing rate: %d docs/sec.\n", rate)

	rv := struct {
		Status string `json:"status"`
	}{
		Status: "ok",
	}
	mustEncode(w, rv)
}