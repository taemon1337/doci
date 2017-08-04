<template>
  <div>
    <h3 class='title'>Search Results:</h3>
    <article v-for="(hit, index) in hits" key="index">
      <strong>{{ hit.id }} in {{ hit.index }}</strong>
      <div v-if="hit.fields && hit.fields.messageClass && hit.fields.messageClass.startsWith('IPM.Note')">
        <email-hit :email='hit'></email-hit>
      </div>
      <div v-else>
        <pre>{{ hit.fields }}</pre>
      </div>
    </article>
  </div>
</template>

<script>
  import { mapGetters } from 'vuex'
  import { SearchTypes } from '@/store/mutation-types'
  import EmailHit from '@/components/EmailHit'

  export default {
    name: 'SearchHits',
    data () {
      return {}
    },
    computed: {
      ...mapGetters({
        hits: SearchTypes.all
      })
    },
    components: {
      EmailHit
    }
  }
</script>
