
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
  axios: {
    /*
     * Can't easily setup axios `browserBaseURL` and `baseUrl`,
     * either in here or from `publicRuntimeConfig` and
     * `privateRuntimeConfig`.
     * https://axios.nuxtjs.org/options/
     * The reason is because we have multiple APIs, and override
     * `baseUrl` in code (e.g. for auth server) seems cause the
     * header to not propogate out (it is a known bug:
     * https://github.com/axios/axios/issues/466)
     * People are also discussion to use proxy to support multiple
     * APIs. Not investigate in that direction yet.
     */
  },
  /*
   * https://nuxtjs.org/docs/2.x/directory-structure/nuxt-config#runtimeconfig
   */
  publicRuntimeConfig: {
    authServer: process.env.AUTH_SERVER || 'http://localhost:8080',
    apiServer: process.env.API_SERVER || 'http://localhost:8080',
  },
  privateRuntimeConfig: {
  },
  /*
  ** Build configuration
  ** See https://nuxtjs.org/api/configuration-build/
  */
  build: {
  }
};
