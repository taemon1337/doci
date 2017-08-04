<template>
  <div class="columns">
    <div class="column is-one-quarter"></div>
    <div class="column is-half">
      <div>
        <div class="field has-addons">
          <p class="control">
            <input @keypress='onSearch' class="input" type="text" placeholder="search...">
          </p>
          <p class="control">
            <span class="select">
              <select @change='setIndex'>
                <option selected>-- select index --</option>
                <option v-for="(opt, index) in indexes" key='index' :value='opt'>{{ opt }}</option>
              </select>
            </span>
          </p>
        </div>
      </div>
    </div>
    <div class="column is-one-quarter"></div>
  </div>
</template>

<script>
  import { mapGetters } from 'vuex'
  import { IndexTypes, SearchTypes } from '@/store/mutation-types'

  export default {
    name: 'SearchForm',
    data () {
      return {
        index: null
      }
    },
    methods: {
      onSearch (e) {
        if (e.which === 13 && e.target.value.length > 2) {
          this.$store.dispatch(SearchTypes.query, { index: this.index, query: { query: e.target.value } })
        }
      },
      setIndex (e) {
        this.index = e.target.value
      }
    },
    computed: {
      ...mapGetters({
        indexes: IndexTypes.all
      })
    },
    created () {
      this.$store.dispatch(IndexTypes.all)
    }
  }
</script>
