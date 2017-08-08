package main
import (
	"fmt"
	"os"
	"path/filepath"
	"strconv"
	"sync"
	"github.com/blevesearch/bleve"
	"github.com/blevesearch/bleve/mapping"
)

// Indexer represents the indexing engine.
type Indexer struct {
	path    string // Path to bleve storage
	batchSz int    // Indexing batch size

	shards  []bleve.Index    // Index shards i.e. bleve indexes
	alias   bleve.IndexAlias // All bleve indexes as one reference, for search
	mapping mapping.IndexMapping
}

var indexNameMapping map[string]Indexer
var indexNameMappingLock sync.RWMutex

// New returns a new indexer.
func NewIndexer(path string, nShards, batchSz int, mapping mapping.IndexMapping) *Indexer {
	return &Indexer{
		path:    path,
		batchSz: batchSz,
		shards:  make([]bleve.Index, 0, nShards),
		alias:   bleve.NewIndexAlias(),
		mapping: mapping,
	}
}

func FindOrCreateIndex(path string, mapping mapping.IndexMapping) (bleve.Index, error) {
  if _, _err := os.Stat(path); _err == nil {
	  return bleve.Open(path) // index already exists, so open it
	} else {
	  return bleve.New(path, mapping) // index does not exist, so create it
	}
}

// Open opens the indexer, preparing it for indexing.
func (i *Indexer) Open() error {
	if err := os.MkdirAll(i.path, 0755); err != nil {
		return fmt.Errorf("unable to create index directory %s", i.path)
	}

	for s := 0; s < cap(i.shards); s++ {
		path := filepath.Join(i.path, strconv.Itoa(s))
		
		b, err := FindOrCreateIndex(path, i.mapping)
		
		if err != nil {
			return fmt.Errorf("index %d at %s: %s", s, path, err.Error())
		}

		i.shards = append(i.shards, b)
		i.alias.Add(b)
	}

	return nil
}

// Index indexes the given docs, dividing the docs evenly across the shards.
// Blocks until all documents have been indexed.
func (i *Indexer) Index(idField string, docs []map[string]interface{}) error {
  total := len(docs)
  nshards := len(i.shards)
	base := 0
	docsPerShard := total / nshards // if 1000 documents, then docsPerShard is 1000 / 40 = 25
	var wg sync.WaitGroup

	wg.Add(nshards)
	for _, s := range i.shards {
		go func(b bleve.Index, ds []map[string]interface{}) {
		  bt := len(ds)
		  fmt.Println("Processing %d documents...", bt)
			defer wg.Done()

			batch := b.NewBatch()
			n := 0

			// Just index whole batches.
			for n = 0; n < bt; n++ {
			// for n = 0; n < total - (total % i.batchSz); n++ {
			  fmt.Println("Batch Number %d / %d", n, bt)
			  var doc = ds[n]
			  var docID = doc[idField].(string)
			  if docID != "" {
				  if err := batch.Index(docID, doc); err != nil {
					  panic(fmt.Sprintf("failed to index doc: %s", err.Error()))
				  }
			  } else {
			    fmt.Println("Missing Document ID for %d", n)
			  }

				if batch.Size() == bt {
					if err := b.Batch(batch); err != nil {
						panic(fmt.Sprintf("failed to index batch: %s", err.Error()))
					}
					batch = b.NewBatch()
				} else {
				  fmt.Println("Batch Size does not equal batchSize! %d != %d", batch.Size(), bt)
				}
			}
		}(s, docs[base:base+docsPerShard])
		base = base + docsPerShard
	}

	wg.Wait()
	return nil
}

// Count returns the total number of documents indexed.
func (i *Indexer) Count() (uint64, error) {
	return i.alias.DocCount()
}

func RegisterIndexerName(name string, idx *Indexer) {
  indexNameMappingLock.Lock()
  defer indexNameMappingLock.Unlock()
  
  if indexNameMapping == nil {
    indexNameMapping = make(map[string]Indexer)
  }
  
  indexNameMapping[name] = *idx
}

func UnregisterIndexerByName(name string) *Indexer {
	indexNameMappingLock.Lock()
	defer indexNameMappingLock.Unlock()

  if indexNameMapping == nil {
    return nil
  }

  rv := indexNameMapping[name]
  if &rv != nil {
		delete(indexNameMapping, name)
	}
	return &rv
}

func IndexerByName(name string) *Indexer {
	indexNameMappingLock.RLock()
	defer indexNameMappingLock.RUnlock()
	tmp := indexNameMapping[name]
	return &tmp
}

func IndexerNames() []string {
	indexNameMappingLock.RLock()
	defer indexNameMappingLock.RUnlock()

	rv := make([]string, len(indexNameMapping))
	count := 0
	for k := range indexNameMapping {
		rv[count] = k
		count++
	}
	return rv
}
