package main

import (
	"net/http"
)

type ListIndexesHandler struct {
}

func NewListIndexesHandler() *ListIndexesHandler {
	return &ListIndexesHandler{}
}

func (h *ListIndexesHandler) ServeHTTP(w http.ResponseWriter, req *http.Request) {
	indexNames := IndexerNames()
	rv := struct {
		Status  string   `json:"status"`
		Indexes []string `json:"indexes"`
	}{
		Status:  "ok",
		Indexes: indexNames,
	}
	mustEncode(w, rv)
}