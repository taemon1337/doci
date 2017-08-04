<template>
  <div id="app">
    <div class="container">
      <navbar></navbar>
      <router-view></router-view>
    </div>
  </div>
</template>

<script>
  import 'bulma'
  import 'font-awesome/css/font-awesome.css'
  import Navbar from '@/components/Navbar'

  export default {
    name: 'app',
    components: {
      Navbar
    }
  }
</script>
