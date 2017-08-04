import axios from 'axios'

let pst = axios.create({
  baseURL: '/upload',
  timeout: 500000
})

let defaults = {
  search: {
    explain: true,
    size: 10,
    from: 0,
    highlight: {},
    fields: ['*']
  }
}

let bleve = {
  list: function () {
    return axios.get('/api')
  },
  searchIndex: function (indexName, opts) {
    let options = Object.assign({}, defaults.search, opts)
    return axios.post('/api/' + indexName + '/_search', options)
  },
  createIndex: function (indexName) {
    return axios.put('/api/' + indexName)
  },
  createDocument: function (indexName, docId, doc) {
    return axios.put('/api/' + indexName + '/' + docId, doc)
  },
  createDocumentBulk: function (indexName, docIdField, docs, bs, progress) {
    let total = docs.length
    let count = 0

    let next = function (success, failure, progress) {
      let batch = docs.splice(0, bs).map(function (item) {
        return Object.assign({}, item, { body: window.atob(item.body) })
      })
      count += batch.length
      if (batch.length) {
        axios.post('/_bulk/' + indexName + '/' + docIdField, batch)
        .then(function (resp) {
          console.log('Progress: ' + count + ' / ' + total)
          if (progress && typeof progress === 'function') {
            progress(resp, Math.floor(count / total * 100))
          }
          next(success, failure, progress)
        })
        .catch(failure)
      } else {
        console.log('Completed: ' + count + ' / ' + total)
        success()
      }
    }

    return new Promise(function (resolve, reject) {
      next(resolve, reject, progress)
    })
  }
}

let uploadPST = function (formdata) {
  return pst.post('/', formdata)
}

export default {
  uploadPST: uploadPST,
  pst: pst,
  bleve: bleve
}
