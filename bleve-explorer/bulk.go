package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	bleveHttp "github.com/blevesearch/bleve/http"
)

type varLookupFunc func(req *http.Request) string

type BulkDocIndexHandler struct {
	defaultIndexName string
	IndexNameLookup  varLookupFunc
	DocIDLookup      varLookupFunc
}

func NewBulkDocIndexHandler(defaultIndexName string) *BulkDocIndexHandler {
	return &BulkDocIndexHandler{
		defaultIndexName: defaultIndexName,
	}
}

func (h *BulkDocIndexHandler) ServeHTTP(w http.ResponseWriter, req *http.Request) {

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
	
	batch := index.NewBatch()

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
	// var docs []interface{}
	var docs []map[string]interface{}
	err = json.Unmarshal(requestBody, &docs)
	if err != nil {
		showError(w, req, fmt.Sprintf("error parsing request body as JSON: %v", err), 400)
		return
	}

  // docs is an array of docs
  for i := range docs {
    var doc = docs[i]
    var docID = doc[idField].(string)
    if docID != "" {
      err = batch.Index(docID, doc)
      if err != nil {
        showError(w, req, fmt.Sprintf("error indexing document '%s': %v", docID, err), 500)
        return
      }
    }
  }
  
  if err := index.Batch(batch); err != nil {
	  showError(w, req, fmt.Sprintf("failed to index batch: %s", err.Error()), 404)
	}

	rv := struct {
		Status string `json:"status"`
	}{
		Status: "ok",
	}
	mustEncode(w, rv)
}