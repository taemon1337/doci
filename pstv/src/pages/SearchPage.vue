<template>
  <div>
    <section class="hero is-primary">
      <div class="hero-body">
        <div class="container">
          <search-form></search-form>
        </div>
      </div>
    </section>
    <search-hits></search-hits>
  </div>
</template>

<script>
  import SearchForm from '@/components/SearchForm'
  import SearchHits from '@/components/SearchHits'

  export default {
    name: 'SearchPage',
    data () {
      return {}
    },
    components: {
      SearchForm,
      SearchHits
    }
  }
</script>
