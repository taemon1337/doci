import Vue from 'vue'
import Router from 'vue-router'
import UploadPage from '@/pages/UploadPage'
import SearchPage from '@/pages/SearchPage'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'search',
      component: SearchPage
    },
    {
      path: '/upload',
      name: 'upload',
      component: UploadPage
    }
  ]
})
