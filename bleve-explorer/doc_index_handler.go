package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
)

type DocIndexHandler struct {
	defaultIndexName string
	IndexNameLookup  varLookupFunc
	DocIDLookup      varLookupFunc
}

func NewDocIndexHandler(defaultIndexName string) *DocIndexHandler {
	return &DocIndexHandler{
		defaultIndexName: defaultIndexName,
	}
}

func (h *DocIndexHandler) ServeHTTP(w http.ResponseWriter, req *http.Request) {

	// find the index to operate on
	var indexName string
	if h.IndexNameLookup != nil {
		indexName = h.IndexNameLookup(req)
	}
	if indexName == "" {
		indexName = h.defaultIndexName
	}
	index := IndexerByName(indexName)
	if index == nil {
		showError(w, req, fmt.Sprintf("no such index '%s'", indexName), 404)
		return
	}

	// find the doc id
	var docID string
	if h.DocIDLookup != nil {
		docID = h.DocIDLookup(req)
	}
	if docID == "" {
		showError(w, req, "document id cannot be empty", 400)
		return
	}

	// read the request body
	requestBody, err := ioutil.ReadAll(req.Body)
	if err != nil {
		showError(w, req, fmt.Sprintf("error reading request body: %v", err), 400)
		return
	}

	// parse request body as json
	var doc interface{}
	err = json.Unmarshal(requestBody, &doc)
	if err != nil {
		showError(w, req, fmt.Sprintf("error parsing request body as JSON: %v", err), 400)
		return
	}

	err = index.alias.Index(docID, doc)
	if err != nil {
		showError(w, req, fmt.Sprintf("error indexing document '%s': %v", docID, err), 500)
		return
	}

	rv := struct {
		Status string `json:"status"`
	}{
		Status: "ok",
	}
	mustEncode(w, rv)
}