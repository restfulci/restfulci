
export default {
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
    '@nuxtjs/auth-next',
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
  auth: {
    strategies: {
      local: false,
      keycloak: {
        scheme: 'oauth2',
        endpoints: {
          authorization: 'http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/auth',
          token: 'http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/token',
          userInfo: 'http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/userinfo',
          logout: 'http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/logout'
        },
        token: {
          property: 'access_token',
          type: 'Bearer',
          name: 'Authorization',
          maxAge: 1800
        },
        refreshToken: {
          property: 'refresh_token',
          maxAge: 60 * 60 * 24 * 30
        },
        responseType: 'code',
        grantType: 'authorization_code',
        clientId: 'job-frontend',
        scope: ['openid', 'profile', 'email'],
        codeChallengeMethod: 'S256',
        // responseType: 'token',
        // grantType: 'authorization_code',
        // accessType: undefined,
        // redirectUri: undefined,
        // logoutRedirectUri: undefined,
        // clientId: 'job-frontend',
        // scope: ['openid', 'profile', 'email'],
        // state: 'UNIQUE_AND_NON_GUESSABLE',
        // codeChallengeMethod: '',
        // responseMode: '',
        // acrValues: '',
        // autoLogout: false
      },
      /*
       * TODO:
       * Attempted universal login.
       * Currently v4 (see below) errors out with
       * > {"error":"invalid_request","error_description":"Missing form parameter: grant_type"}%
       * Above at least has some grantType to setup, but it is under v5/dev,
       * and people are talking about its problems.
       * https://github.com/nuxt-community/auth-module/issues/559 should be
       * a very important resource on how to set it up. I'd just wait until
       * v5 is officially supported, and give it another try.
       */
    },
  },
  router: {
    middleware: ['auth']
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
