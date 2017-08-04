import Vue from 'vue'
import Vuex from 'vuex'
import * as actions from './actions'
import * as getters from './getters'
import messages from './modules/messages'
import statuses from './modules/statuses'
import workflows from './modules/workflows'
import indexes from './modules/indexes'
import searches from './modules/searches'

Vue.use(Vuex)

const debug = process.env.NODE_ENV !== 'production'

export default new Vuex.Store({
  actions,
  getters,
  modules: {
    messages,
    workflows,
    statuses,
    indexes,
    searches
  },
  strict: debug
})
