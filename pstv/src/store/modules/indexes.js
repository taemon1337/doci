import { IndexTypes } from '../mutation-types'
import Api from '@/api'

const state = {
  all: []
}

// getters
const getters = {
  [IndexTypes.all]: state => state.all
}

// actions
const actions = {
  [IndexTypes.all] ({ commit }) {
    Api.bleve.list().then(function (resp) {
      commit(IndexTypes.all, resp.data.indexes)
    })
    .catch(function (err) {
      console.warn('Error loading indexes ', err)
    })
  }
}

// mutations must be synchronous
const mutations = {
  [IndexTypes.all] (state, indexes) {
    state.all = indexes
  }
}

export default {
  state,
  getters,
  actions,
  mutations
}
