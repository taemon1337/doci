<template>
  <article>
    <p class='title'>{{ email.fields.subject }}</p>
    <p class='subtitle'>From: {{ email.fields.sender }} <br>To: {{ email.fields.receiver }}</p>
    
    <button @click='decodeBody' class='button'>Show Content</button>
    <pre v-if="body">{{ body }}</pre>
    <pre v-else>{{ email.fields }}</pre>
  </article>
</template>

<script>
  export default {
    name: 'EmailHit',
    props: {
      email: {
        type: Object,
        required: true
      }
    },
    data () {
      return {
        body: null
      }
    },
    methods: {
      decodeBody () {
        this.body = window.btoa(this.email.body)
      }
    }
  }
</script>
