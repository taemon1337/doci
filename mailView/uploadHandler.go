import (
  "net/http"
  "os"
)

type CreateUploadHandler struct {
  basePath  string
}

func NewCreateIndexHandler(basePath string) *CreateUploadHandler {
  return &CreateUploadHandler{
    basePath: basePath,
  }
}

func (h *CreateUploadHandler) ServeHTTP(w http.ResponseWriter, req *http.Request) {
  // https://github.com/blevesearch/bleve/blob/master/http/index_create.go
}