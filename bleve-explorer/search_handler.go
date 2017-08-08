package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"

	"github.com/blevesearch/bleve"
	"github.com/blevesearch/bleve/search/query"
)

// SearchHandler can handle search requests sent over HTTP
type SearchHandler struct {
	defaultIndexName string
	IndexNameLookup  varLookupFunc
}

func NewSearchHandler(defaultIndexName string) *SearchHandler {
	return &SearchHandler{
		defaultIndexName: defaultIndexName,
	}
}

func (h *SearchHandler) ServeHTTP(w http.ResponseWriter, req *http.Request) {

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

	// read the request body
	requestBody, err := ioutil.ReadAll(req.Body)
	if err != nil {
		showError(w, req, fmt.Sprintf("error reading request body: %v", err), 400)
		return
	}

	fmt.Printf("request body: %s", requestBody)

	// parse the request
	var searchRequest bleve.SearchRequest
	err = json.Unmarshal(requestBody, &searchRequest)
	if err != nil {
		showError(w, req, fmt.Sprintf("error parsing query: %v", err), 400)
		return
	}

	fmt.Printf("parsed request %#v", searchRequest)

	// validate the query
	if srqv, ok := searchRequest.Query.(query.ValidatableQuery); ok {
		err = srqv.Validate()
		if err != nil {
			showError(w, req, fmt.Sprintf("error validating query: %v", err), 400)
			return
		}
	}

	// execute the query
	searchResponse, err := index.alias.Search(&searchRequest)
	if err != nil {
		showError(w, req, fmt.Sprintf("error executing query: %v", err), 500)
		return
	}

	// encode the response
	mustEncode(w, searchResponse)
}