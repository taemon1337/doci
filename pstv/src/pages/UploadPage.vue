<template>
  <div class="columns">
    <div class="column is-half">
      <h3 class='title'>
        PST Upload
        <span class='subtitle'>To be parsed and indexed</span>
      </h3>
      <pst-form></pst-form>
    </div>
    <div class="column is-half">
      <h3 class='title'>
        Status:
      </h3>
      <pst-status></pst-status>
    </div>
  </div>
</template>

<script>
  import PstForm from '@/components/PstForm'
  import PstStatus from '@/components/PstStatus'

  export default {
    name: 'UploadPage',
    data () {
      return {}
    },
    components: {
      PstForm,
      PstStatus
    }
  }
</script>
