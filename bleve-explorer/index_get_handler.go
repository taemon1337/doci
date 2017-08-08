package main

import (
	"fmt"
	"net/http"

	"github.com/blevesearch/bleve/mapping"
)

type GetIndexHandler struct {
	IndexNameLookup varLookupFunc
}

func NewGetIndexHandler() *GetIndexHandler {
	return &GetIndexHandler{}
}

func (h *GetIndexHandler) ServeHTTP(w http.ResponseWriter, req *http.Request) {
	// find the name of the index to create
	var indexName string
	if h.IndexNameLookup != nil {
		indexName = h.IndexNameLookup(req)
	}
	if indexName == "" {
		showError(w, req, "index name is required", 400)
		return
	}

	index := IndexerByName(indexName)
	if index == nil {
		showError(w, req, fmt.Sprintf("no such index '%s'", indexName), 404)
		return
	}

	rv := struct {
		Status  string               `json:"status"`
		Name    string               `json:"name"`
		Mapping mapping.IndexMapping `json:"mapping"`
	}{
		Status:  "ok",
		Name:    indexName,
		Mapping: index.alias.Mapping(),
	}
	mustEncode(w, rv)
}