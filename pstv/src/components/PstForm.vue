<template>
  <form @submit.prevent.stop='send' :action='action' :method='method' ref='form'>
    <div class="field">
      <input @change='fileSelected' name='file' class="is-hidden" type="file" ref='fileInput' multiple required>
      <label class="label">PST file</label>
      <small>*optionally include .p12 and .md5 files as well</small>
      <p class="control has-icons-left has-icons-right">
        <input @click='openFileDialog' class="input" type="text" placeholder="Select PST file, p12, and md5" ref='fakeInput'>
        <span class="icon is-small is-left">
          <i class="fa fa-file"></i>
        </span>
      </p>
    </div>

    <div class="field">
      <label class="label">Index Name</label>
      <div class="control">
        <input name='index' class="input" type="text" placeholder="The name of the indexed data for this PST..." ref='indexInput'>
      </div>
    </div>
    
    <progress v-if="progress" class="progress is-success" :value="progress" max="100">{{ progress }}%</progress>
    
    <div class="field is-grouped">
      <div class="control">
        <button type='submit' class="button is-primary">Submit</button>
      </div>
      <div class="control">
        <button @click='reset' type='button' class="button is-link">Cancel</button>
      </div>
    </div>
  </form>
</template>

<script>
  // import serializeForm from '@/lib/serializeForm'
  import Api from '@/api'
  import { WorkflowTypes } from '@/store/mutation-types'
  import { mapGetters } from 'vuex'

  export default {
    name: 'PstForm',
    data () {
      return {
        progress: null,
        action: 'http://127.0.0.1:8888/upload',
        method: 'POST'
      }
    },
    computed: {
      ...mapGetters({
        steps: WorkflowTypes.pst,
        currentstep: WorkflowTypes.currentpst
      })
    },
    methods: {
      openFileDialog () {
        this.$refs.fileInput.click()
      },
      fileSelected (e) {
        let names = []
        for (let i = 0; i < e.target.files.length; i += 1) {
          let file = e.target.files[i]
          if (file.name.match(/\.pst$/i)) {
            names.push(file.name)
            if (this.$refs.indexInput.value === '') {
              this.$refs.indexInput.value = file.name
            }
            this.$store.dispatch(WorkflowTypes.finish, 'pst', 'Selected ' + file.name)
          } else if (file.name.match(/\.p12/i)) {
            names.push(file.name)
          } else if (file.name.match(/\.md5/i)) {
            names.push(file.name)
          }
        }
        this.$refs.fakeInput.value = names.join(', ')
      },
      send (e) {
        console.log('sending form...')
        let self = this
        let formdata = new FormData(e.target)
        let indexName = formdata.get('index')
        if (indexName) {
          self.progress = 1
          self.$store.dispatch(WorkflowTypes.nextStep, 'pst', 'creating ' + indexName)
          Api.bleve.createIndex(indexName).then(function (resp) {
            self.$store.dispatch(WorkflowTypes.finish, 'pst', '<pre>' + JSON.stringify(resp.data, null, 2) + '</pre>')
            self.$store.dispatch(WorkflowTypes.nextStep, 'pst', 'Uploading PST...')
            Api.uploadPST(formdata).then(function (resp) {
              if (resp.data && resp.data.emails) {
                self.$store.dispatch(WorkflowTypes.finish, 'pst', 'Found ' + resp.data.emails.length + ' email messages.')
                self.$store.dispatch(WorkflowTypes.nextStep, 'pst', 'Indexing ' + resp.data.emails.length + ' messages...')
                Api.bleve.createDocumentBulk(indexName, 'messageId', resp.data.emails, 200, function (res, pct) {
                  self.progress = pct
                  console.log('RESP: ', res.data)
                })
                .then(function (resp) {
                  self.progress = 100
                  self.$store.dispatch(WorkflowTypes.finish, 'pst', 'Completed batch message index')
                })
                .catch(function (err) {
                  self.$store.dispatch(WorkflowTypes.error, 'pst', 'Failed to index messages: ' + err.toString())
                })
              } else {
                self.$store.dispatch(WorkflowTypes.error, 'pst', 'Invalid Response! Expected emails in response.')
              }
            })
            .catch(function (err) {
              self.$store.dispatch(WorkflowTypes.error, 'pst', 'Error uploading PST: ' + err.toString())
            })
          })
          .catch(function (err) {
            self.$store.dispatch(WorkflowTypes.error, 'pst', 'Error creating index: ' + err.toString())
          })
        } else {
          self.$store.dispatch(WorkflowTypes.error, 'pst', 'No Index name provided!')
        }
      },
      reset () {
        this.$store.dispatch(WorkflowTypes.reset, 'pst')
        this.$refs.form.reset()
      }
    }
  }
</script>
