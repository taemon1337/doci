package main

import (
  "encoding/json"
  "flag"

  "github.com/blevesearch/bleve"
  bleveHttp "github.com/blevesearch/bleve/http"
)

var batchSize = flag.Int("batchSize", 100, "batch size for indexing")
var bindAddr = flag.String("addr", ":8094", "http listen address")
var jsonDir = flag.String("jsonDir", "data/", "json directory")
var indexPath = flag.String("index", "mailView.bleve", "index path")
var staticEtag = flag.String("staticEtag", "", "A static etag value.")
var staticPath = flag.String("static", "static/", "Path to the static content")
var cpuprofile = flag.String("cpuprofile", "", "write cpu profile to file")
var memprofile = flag.String("memprofile", "", "write mem profile to file")

func main() {
  flag.Parse()
  
  log.Printf("GOMAXPROCS: %d", runtime.GOMAXPROCS(-1))

  if *cpuprofile != "" {
    f, err := os.Create(*cpuprofile)
    if err != nil {
      log.Fatal(err)
    }
    pprof.StartCPUProfile(f)
  }

  mailIndex, err := bleve.Open(*indexPath)
  if err == bleve.ErrorIndexPathDoesNotExist {
    log.Printf("Creating new index...")
    indexMapping, err := buildIndexMapping()
    if err != nil {
      log.Fatal(err)
    }
    mailIndex, err = bleve.New(*indexPath, indexMapping)
    if err != nil {
      log.Fatal(err)
    }
    
    go func() {
      err = indexMail(mailIndex)
      if err != nil {
        log.Fatal(err)
      }
      pprof.StopCPUProfile()
      if *memprofile != "" {
        f, err := os.Create(*memprofile)
        if err != nil {
          log.Fatal(err)
        }
        pprof.WriteHeapProfile(f)
        f.close()
      }
    }()
  } else if err != nil {
    log.Fatal(err)
  } else {
    log.Printf("Opening existing index...")
  }
  
  route := staticFileRouter()
  
  bleveHttp.RegisterIndexName("mail", mailIndex)
  searchHandler := bleveHttp.NewSearchHandler("mail")
  router.Handle("/api/search", searchHandler).Methods("POST")
  listFieldsHandler := bleveHttp.NewListFieldsHandler("mail")
  router.Handle("/api/fields", listFieldsHandler).Methods("GET")
  
  debugHandler := bleveHttp.NewDebugDocumentHandler("mail")
  debugHandler.DocIDLookup = docIDLookup
  router.Handle("/api/debug/{docID}", debugHandler).Methods("GET")
  
  http.Handle("/", router)
  log.Printf("Listening on %v", *bindAddr)
  log.Fatal(http.ListenAndServe(*bindAddr, nil))
}

func indexMail(i bleve.Index) error {
  
}
