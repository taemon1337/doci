package main

import (
	"fmt"
	"net/http"
)

type DocCountHandler struct {
	defaultIndexName string
	IndexNameLookup  varLookupFunc
}

func NewDocCountHandler(defaultIndexName string) *DocCountHandler {
	return &DocCountHandler{
		defaultIndexName: defaultIndexName,
	}
}

func (h *DocCountHandler) ServeHTTP(w http.ResponseWriter, req *http.Request) {
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

	docCount, err := index.Count()
	if err != nil {
		showError(w, req, fmt.Sprintf("error counting docs: %v", err), 500)
		return
	}
	rv := struct {
		Status string `json:"status"`
		Count  uint64 `json:"count"`
	}{
		Status: "ok",
		Count:  docCount,
	}
	mustEncode(w, rv)
}