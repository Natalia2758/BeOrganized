import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './app/App.vue'
import router from './app/router'
import http from '@/common/api/http'

import './assets/styles/main.css'

import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import 'bootstrap-icons/font/bootstrap-icons.css'

/** Creates app with a main Vue component - App, mounting it to DOM. */
async function bootstrap() {
  try {
    await http.get('/auth/csrf')
  } catch (error) {
    console.error('Failed to initialize CSRF token', error)
  }

  const app = createApp(App)

  app.use(createPinia())
  app.use(router)

  app.mount('#app')
}

void bootstrap()
