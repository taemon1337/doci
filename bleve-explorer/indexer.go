package main
import (
	"fmt"
	"os"
	"path/filepath"
	"strconv"
	"sync"
  "github.com/blevesearch/bleve/mapping"
	"github.com/blevesearch/bleve"
)

// Indexer represents the indexing engine.
type Indexer struct {
	path    string // Path to bleve storage
	batchSz int    // Indexing batch size

	shards []bleve.Index    // Index shards i.e. bleve indexes
	alias  bleve.IndexAlias // All bleve indexes as one reference, for search
}

// New returns a new indexer.
func NewIndexer(path string, nShards, batchSz int) *Indexer {
	return &Indexer{
		path:    path,
		batchSz: batchSz,
		shards:  make([]bleve.Index, 0, nShards),
		alias:   bleve.NewIndexAlias(),
	}
}

// Open opens the indexer, preparing it for indexing.
func (i *Indexer) Open() error {
	if err := os.MkdirAll(i.path, 0755); err != nil {
		return fmt.Errorf("unable to create index directory %s", i.path)
	}

	for s := 0; s < cap(i.shards); s++ {
		path := filepath.Join(i.path, strconv.Itoa(s))
		b, err := bleve.New(path, getMapping())
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
	base := 0
	docsPerShard := (len(docs) / len(i.shards))
	var wg sync.WaitGroup

	wg.Add(len(i.shards))
	for _, s := range i.shards {
		go func(b bleve.Index, ds []map[string]interface{}) {
			defer wg.Done()

			batch := b.NewBatch()
			n := 0

			// Just index whole batches.
			for n = 0; n < len(ds)-(len(ds)%i.batchSz); n++ {
			  fmt.Println("Batch Number %d / %d", n, total)
			  var doc = ds[n]
			  var docID = doc[idField].(string)
			  if docID != "" {
				  if err := batch.Index(docID, doc); err != nil {
					  panic(fmt.Sprintf("failed to index doc: %s", err.Error()))
				  }
			  }

				if batch.Size() == i.batchSz {
					if err := b.Batch(batch); err != nil {
						panic(fmt.Sprintf("failed to index batch: %s", err.Error()))
					}
					batch = b.NewBatch()
				} else {
				  fmt.Println("Batch Size does not equal batchSize! %d != %d", batch.Size(), i.batchSz)
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

func getMapping() mapping.IndexMapping {
	// a generic reusable mapping for english text
	standardJustIndexed := bleve.NewTextFieldMapping()
	standardJustIndexed.Store = false
	standardJustIndexed.IncludeInAll = false
	standardJustIndexed.IncludeTermVectors = false
	standardJustIndexed.Analyzer = "standard"

	articleMapping := bleve.NewDocumentMapping()

	// body
	articleMapping.AddFieldMappingsAt("Body", standardJustIndexed)

	indexMapping := bleve.NewIndexMapping()
	indexMapping.DefaultMapping = articleMapping
	indexMapping.DefaultAnalyzer = "standard"
	return indexMapping
}
