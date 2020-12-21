
export default {
  /*
  ** Nuxt rendering mode
  ** See https://nuxtjs.org/api/configuration-mode
  */
  mode: 'universal',
  /*
  ** Nuxt target
  ** See https://nuxtjs.org/api/configuration-target
  */
  target: 'server',
  /*
   * This is for build time (instead of runtime) variables.
   * https://nuxtjs.org/docs/2.x/directory-structure/nuxt-config#env
   */
  env: {
  },
  /*
  ** Headers of the page
  ** See https://nuxtjs.org/api/configuration-head
  */
  head: {
    title: 'RestfulCI' || process.env.npm_package_name || '',
    meta: [
      { charset: 'utf-8' },
      { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      { hid: 'description', name: 'description', content: process.env.npm_package_description || '' }
    ],
    link: [
      { rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' },
      { rel: 'stylesheet', type: 'text/css', href: '/css/style.css' }
    ]
  },
  /*
  ** Global CSS
  */
  css: [
  ],
  /*
  ** Plugins to load before mounting the App
  ** https://nuxtjs.org/guide/plugins
  */
  plugins: [
  ],
  /*
  ** Auto import components
  ** See https://nuxtjs.org/api/configuration-components
  */
  components: true,
  /*
  ** Nuxt.js dev-modules
  */
  buildModules: [
  ],
  /*
  ** Nuxt.js modules
  */
  modules: [
    '@nuxtjs/axios',
  ],
  /*
   * https://axios.nuxtjs.org/options/
   */
  axios: {
    /*
     * Used as fallback if no runtime config is provided.
     * Environment variable API_URL can be used to override baseURL.
     */
    // baseURL: process.env.API_URL || 'http://localhost:8080'
    baseURL: 'http://localhost:8080'
  },
  /*
   * https://nuxtjs.org/docs/2.x/directory-structure/nuxt-config#runtimeconfig
   */
  publicRuntimeConfig: {
    authServer: process.env.AUTH_SERVER || 'http://localhost:8880',
    axios: {
      baseURL: process.env.BASE_URL
      // browserBaseURL: process.env.BROWSER_BASE_URL
    }
  },
  privateRuntimeConfig: {
    axios: {
      baseURL: process.env.BASE_URL
    }
  },
  /*
  ** Build configuration
  ** See https://nuxtjs.org/api/configuration-build/
  */
  build: {
  }
};
