package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"os"

	"github.com/blevesearch/bleve"
)

type CreateIndexHandler struct {
	basePath        string
	batchSz         int    // Indexing batch size
	nShards         int    // number of shards
	IndexNameLookup varLookupFunc
	alias           bleve.Index
}

func NewCreateIndexHandler(basePath string, nShards int, batchSz int) *CreateIndexHandler {
	return &CreateIndexHandler{
		basePath: basePath,
		batchSz:  batchSz,
		nShards:  nShards,
		alias:    bleve.NewIndexAlias(),
	}
}

func (h *CreateIndexHandler) ServeHTTP(w http.ResponseWriter, req *http.Request) {
	// find the name of the index to create
	var indexName string
	if h.IndexNameLookup != nil {
		indexName = h.IndexNameLookup(req)
	}
	if indexName == "" {
		showError(w, req, "index name is required", 400)
		return
	}

	indexMapping := getMapping()

	// read the request body
	requestBody, err := ioutil.ReadAll(req.Body)
	if err != nil {
		showError(w, req, fmt.Sprintf("error reading request body: %v", err), 400)
		return
	}

	// interpret request body as index mapping
	if len(requestBody) > 0 {
		err := json.Unmarshal(requestBody, &indexMapping)
		if err != nil {
			showError(w, req, fmt.Sprintf("error parsing index mapping: %v", err), 400)
			return
		}
	}

  i := NewIndexer(h.indexPath(indexName), h.nShards, h.batchSz, indexMapping)
  if err := i.Open(); err != nil {
    showError(w, req, fmt.Sprintf("failed to open indexer: ", err), 404)
    return
  }

  i.alias.SetName(indexName)
  RegisterIndexerName(indexName, i)

	rv := struct {
		Status string `json:"status"`
	}{
		Status: "ok",
	}
	mustEncode(w, rv)
}

func (h *CreateIndexHandler) indexPath(name string) string {
	return h.basePath + string(os.PathSeparator) + name
}