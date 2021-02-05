
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
    /*
     * Currently v4 (https://auth.nuxtjs.org/status) errors out with
     * > {"error":"invalid_request","error_description":"Missing form parameter: grant_type"}%
     *
     * v5 is working. Here uses v5.
     * https://github.com/nuxt-community/auth-module/issues/559 is a good
     * resource on how to setup KeyCloak auth on v5.
     *
     * Note:
     * To test locally, we may need to manually go into KeyCload admin console
     * and change redirectUri there.
     */
    strategies: {
      local: false,
      keycloak: {
        scheme: 'oauth2',
        endpoints: {
          authorization: process.env.AUTH_SERVER + '/auth/realms/restfulci/protocol/openid-connect/auth',
          token: process.env.AUTH_SERVER + '/auth/realms/restfulci/protocol/openid-connect/token',
          userInfo: process.env.AUTH_SERVER + '/auth/realms/restfulci/protocol/openid-connect/userinfo',
          /*
           * Using `logoutRedirectUri` in this config doesn't work, as that will end up with
           * `?logout_uri=...` while KeyCloak expect `redirect_uri=...`. This trick can make
           * logout working again.
           * Refer: https://github.com/nuxt-community/auth-module/issues/559#issuecomment-676348616
           */
          logout: process.env.AUTH_SERVER + '/auth/realms/restfulci/protocol/openid-connect/logout?redirect_uri=' + process.env.FRONTEND_URI + '/login',
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
        accessType: undefined,
        redirectUri: undefined, /* this defaults to `/login` */
        logoutRedirectUri: undefined,
        clientId: 'job-frontend',
        scope: ['openid', 'profile', 'email'],
        state: 'UNIQUE_AND_NON_GUESSABLE',
        codeChallengeMethod: 'S256',
        responseMode: '',
        acrValues: '',
      },
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
