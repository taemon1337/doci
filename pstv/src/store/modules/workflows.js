import { WorkflowTypes } from '../mutation-types'

const state = {
  pst: {
    step: 0,
    steps: [
      { id: 1, title: 'Complete form', status: '', content: '' },
      { id: 2, title: 'Create index', status: '', content: '' },
      { id: 3, title: 'Convert PST to text', status: '', content: '' },
      { id: 4, title: 'Index PST messages', status: '', content: '' }
    ]
  }
}

// getters
const getters = {
  [WorkflowTypes.pst]: state => state.pst.steps,
  [WorkflowTypes.currentpst]: state => state.pst.step
}

// actions
const actions = {
  [WorkflowTypes.start] ({ commit }, workflow) {
    commit(WorkflowTypes.start, workflow)
  },
  [WorkflowTypes.finish] ({ commit }, workflow, content) {
    commit(WorkflowTypes.finish, workflow, content)
  },
  [WorkflowTypes.nextStep] ({ commit }, workflow, content) {
    commit(WorkflowTypes.nextStep, workflow, content)
  },
  [WorkflowTypes.error] ({ commit }, workflow, content) {
    commit(WorkflowTypes.error, workflow, content)
  },
  [WorkflowTypes.reset] ({ commit }, workflow) {
    commit(WorkflowTypes.reset, workflow)
  }
}

// mutations must be synchronous
const mutations = {
  [WorkflowTypes.start] (state, workflow) {
    if (state[workflow]) {
      state[workflow].step = 1
      state[workflow].steps[1].status = 'working'
    }
  },
  [WorkflowTypes.finish] (state, workflow, content) {
    if (state[workflow]) {
      let step = state[workflow].steps[state[workflow].step]
      if (step) {
        step.status = 'pass'
        step.content = content
      }
    }
  },
  [WorkflowTypes.nextStep] (state, workflow, content) {
    if (state[workflow]) {
      let current = state[workflow].steps[state[workflow].step]
      if (current.status === 'pass') {
        state[workflow].step += 1
        let nextstep = state[workflow].steps[state[workflow].step]
        if (nextstep) {
          nextstep.status = 'working'
          nextstep.content = content
        }
      } else {
        current.content = 'Cannot continue with failure!'
      }
    }
  },
  [WorkflowTypes.error] (state, workflow, content) {
    if (state[workflow]) {
      let step = state[workflow].steps[state[workflow].step]
      if (step) {
        step.status = 'fail'
        step.content = content
      }
    }
  },
  [WorkflowTypes.reset] (state, workflow) {
    if (state[workflow]) {
      state[workflow].step = 0
      state[workflow].steps.forEach(function (step) {
        step.status = ''
        step.content = ''
      })
    }
  }
}

export default {
  state,
  getters,
  actions,
  mutations
}
