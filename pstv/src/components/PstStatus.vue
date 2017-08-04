<template>
  <div class="columns is-multiline">
    <div class="column is-12" v-for="(step, index) in steps" key="index">
      <task-item :id='step.id' :status='step.status' :title="step.title" :content="step.content"></task-item>
    </div>
  </div>
</template>

<script>
  import { mapGetters } from 'vuex'
  import { WorkflowTypes } from '@/store/mutation-types'
  import TaskItem from '@/components/TaskItem'

  export default {
    name: 'PstStatus',
    data () {
      return {}
    },
    computed: {
      ...mapGetters({
        steps: WorkflowTypes.pst
      })
    },
    components: {
      TaskItem
    }
  }
</script>
