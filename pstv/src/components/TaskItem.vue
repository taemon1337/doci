<template>
  <article class='media'>
    <div class='media-left'>
      <span :class="statusColor">
        <i :class="statusIcon"></i>
      </span>
    </div>
    <div class="media-content">
      <div class="content">
        <p v-if="title" class="title">{{ title }}</p>
        <span v-if="content" class="subtitle">{{ content }}</span>
      </div>
    </div>
    <div class="media-right">
      <span v-if="status === 'working'" class="icon is-large">
        <i class="fa fa-spin fa-spinner"></i>
      </span>
    </div>
  </article>
</template>

<script>
  import { StatusTypes } from '@/store/mutation-types'

  export default {
    name: 'TaskItem',
    props: {
      id: {
        type: [String, Number]
      },
      status: {
        type: String
      },
      title: {
        type: String
      },
      content: {
        type: String
      }
    },
    data () {
      return {}
    },
    computed: {
      statusColor () {
        switch (this.status) {
          case 'pass':
            return 'icon has-text-success is-large'
          case 'fail':
            return 'icon has-text-danger is-large'
          case 'info':
            return 'icon has-text-info is-large'
          case 'warn':
            return 'icon has-text-warn is-large'
          default:
            return 'icon is-large'
        }
      },
      statusIcon () {
        switch (this.status) {
          case 'pass':
            return 'fa fa-check-square-o'
          case 'fail':
            return 'fa fa-exclamation-circle'
          default:
            return 'fa fa-square-o'
        }
      }
    },
    methods: {
      remove () {
        this.$store.dispatch(StatusTypes.remove, this.id)
      }
    }
  }
</script>
