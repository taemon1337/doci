import { SearchTypes } from '../mutation-types'
import Api from '@/api'

const state = {
  all: [],
  query: {
    boost: 1,
    query: null
  }
}

// getters
const getters = {
  [SearchTypes.all]: state => state.all
}

// actions
const actions = {
  [SearchTypes.query] ({ commit }, opts) {
    Api.bleve.searchIndex(opts.index, { query: opts.query }).then(function (resp) {
      commit(SearchTypes.query, resp.data.hits)
    })
    .catch(function (err) {
      console.warn('Error searching ', err)
    })
  }
}

// mutations must be synchronous
const mutations = {
  [SearchTypes.query] (state, hits) {
    state.all = hits
  }
}

export default {
  state,
  getters,
  actions,
  mutations
}
