package main

import (
	"fmt"
	"net/http"
	"time"

	"github.com/blevesearch/bleve/document"
)

type DocGetHandler struct {
	defaultIndexName string
	IndexNameLookup  varLookupFunc
	DocIDLookup      varLookupFunc
}

func NewDocGetHandler(defaultIndexName string) *DocGetHandler {
	return &DocGetHandler{
		defaultIndexName: defaultIndexName,
	}
}

func (h *DocGetHandler) ServeHTTP(w http.ResponseWriter, req *http.Request) {
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

	doc, err := index.alias.Document(docID)
	if err != nil {
		showError(w, req, fmt.Sprintf("error deleting document '%s': %v", docID, err), 500)
		return
	}
	if doc == nil {
		showError(w, req, fmt.Sprintf("no such document '%s'", docID), 404)
		return
	}

	rv := struct {
		ID     string                 `json:"id"`
		Fields map[string]interface{} `json:"fields"`
	}{
		ID:     docID,
		Fields: map[string]interface{}{},
	}
	for _, field := range doc.Fields {
		var newval interface{}
		switch field := field.(type) {
		case *document.TextField:
			newval = string(field.Value())
		case *document.NumericField:
			n, err := field.Number()
			if err == nil {
				newval = n
			}
		case *document.DateTimeField:
			d, err := field.DateTime()
			if err == nil {
				newval = d.Format(time.RFC3339Nano)
			}
		}
		existing, existed := rv.Fields[field.Name()]
		if existed {
			switch existing := existing.(type) {
			case []interface{}:
				rv.Fields[field.Name()] = append(existing, newval)
			case interface{}:
				arr := make([]interface{}, 2)
				arr[0] = existing
				arr[1] = newval
				rv.Fields[field.Name()] = arr
			}
		} else {
			rv.Fields[field.Name()] = newval
		}
	}

	mustEncode(w, rv)
}