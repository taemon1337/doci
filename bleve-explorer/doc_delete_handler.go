package main

import (
	"fmt"
	"net/http"
)

type DocDeleteHandler struct {
	defaultIndexName string
	IndexNameLookup  varLookupFunc
	DocIDLookup      varLookupFunc
}

func NewDocDeleteHandler(defaultIndexName string) *DocDeleteHandler {
	return &DocDeleteHandler{
		defaultIndexName: defaultIndexName,
	}
}

func (h *DocDeleteHandler) ServeHTTP(w http.ResponseWriter, req *http.Request) {

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

	err := index.alias.Delete(docID)
	if err != nil {
		showError(w, req, fmt.Sprintf("error deleting document '%s': %v", docID, err), 500)
		return
	}

	rv := struct {
		Status string `json:"status"`
	}{
		Status: "ok",
	}
	mustEncode(w, rv)
}