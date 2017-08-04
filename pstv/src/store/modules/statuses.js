import { StatusTypes } from '../mutation-types'

const state = {
  all: []
}

// getters
const getters = {
  [StatusTypes.all]: state => state.all
}

// actions
const actions = {
  [StatusTypes.add] ({ commit }, status) {
    commit(StatusTypes.add, status)
  },
  [StatusTypes.update] ({ commit }, status) {
    commit(StatusTypes.update, status)
  },
  [StatusTypes.remove] ({ commit }, idx) {
    commit(StatusTypes.remove, idx)
  },
  [StatusTypes.clear] ({ commit }) {
    commit(StatusTypes.clear)
  }
}

// mutations must be synchronous
const mutations = {
  [StatusTypes.add] (state, status) {
    if (status.title) {
      state.all.push(status)
    } else {
      console.error('Invalid Status, title is required', status)
    }
  },
  [StatusTypes.update] (state, status) {
    if (status.title) {
      state.all.forEach(function (s, i) {
        if (s.title === status.title) {
          state.all.splice(i, 1, status)
        }
      })
    }
  },
  [StatusTypes.remove] (state, idx) {
    state.all.splice(idx, 1)
  },
  [StatusTypes.clear] (state, idx) {
    state.all = []
  }
}

export default {
  state,
  getters,
  actions,
  mutations
}
