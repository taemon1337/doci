<template>
  <nav class="navbar ">
    <div class="navbar-brand">
      <a class="navbar-item" href="http://bulma.io">
        <img :src="logo" alt="Microsoft Outlook 2010">
      </a>
  
      <a class="navbar-item is-hidden-desktop" href="https://github.com/jgthms/bulma" target="_blank">
        <span class="icon" style="color: #333;">
          <i class="fa fa-github"></i>
        </span>
      </a>
  
      <a class="navbar-item is-hidden-desktop" href="https://twitter.com/jgthms" target="_blank">
        <span class="icon" style="color: #55acee;">
          <i class="fa fa-twitter"></i>
        </span>
      </a>
  
      <div class="navbar-burger burger" data-target="navMenuExample">
        <span></span>
        <span></span>
        <span></span>
      </div>
    </div>
  
    <div id="navMenuExample" class="navbar-menu">
      <router-link class="navbar-item" to='/'>Search</router-link>
      <router-link class="navbar-item" to='upload'>Upload</router-link>
      <div class="navbar-end">
        <a class="navbar-item" href="https://github.com/jgthms/bulma" target="_blank">
          Github
        </a>
        <a class="navbar-item" href="https://twitter.com/jgthms" target="_blank">
          Twitter
        </a>
        <div class="navbar-item">
          <div class="field is-grouped">
            <p class="control">
              <a id="twitter"
                class="button"
                data-social-network="Twitter"
                data-social-action="tweet"
                data-social-target="http://bulma.io"
                target="_blank"
                href="https://twitter.com/intent/tweet?text=Bulma: a modern CSS framework based on Flexbox&url=http://bulma.io&via=jgthms">
                <span class="icon">
                  <i class="fa fa-twitter"></i>
                </span>
                <span>Tweet</span>
              </a>
            </p>
            <p class="control">
              <a class="button is-primary" href="https://github.com/jgthms/bulma/archive/0.4.3.zip">
                <span class="icon">
                  <i class="fa fa-download"></i>
                </span>
                <span>Download</span>
              </a>
            </p>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script>
  export default {
    name: 'Navbar',
    data () {
      return {
        logo: require('../assets/outlook_logo.png')
      }
    }
  }
</script>
