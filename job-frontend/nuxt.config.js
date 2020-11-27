
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
    authServer: process.env.AUTH_SERVER || 'http://localhost:8880',
    authClientSecret: process.env.AUTH_CLIENT_SECRET || 'dc80857e-b4b7-45ec-ab56-1242ba7600ff'
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
    '@nuxtjs/auth',
  ],
  axios: {
    baseURL: process.env.API_URL
  },
  auth: {
    strategies: {
      local: false,
      keycloak: {
      //   scheme: 'oauth2',
      //   endpoints: {
      //     authorization: 'http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/auth',
      //     token: 'http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/token',
      //     userInfo: 'http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/userinfo',
      //     logout: 'https://example.com/logout'
      //   },
      //   token: {
      //     property: 'access_token',
      //     type: 'Bearer',
      //     maxAge: 1800
      //   },
      //   refreshToken: {
      //     property: 'refresh_token',
      //     maxAge: 60 * 60 * 24 * 30
      //   },
      //   responseType: 'token',
      //   grantType: 'authorization_code',
      //   accessType: undefined,
      //   redirectUri: undefined,
      //   logoutRedirectUri: undefined,
      //   clientId: 'job-frontend',
      //   scope: ['openid', 'profile', 'email'],
      //   state: 'UNIQUE_AND_NON_GUESSABLE',
      //   codeChallengeMethod: '',
      //   responseMode: '',
      //   acrValues: '',
      //   // autoLogout: false
      // },
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
        _scheme: 'oauth2',
        authorization_endpoint: 'http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/auth',
        userinfo_endpoint: 'http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/userinfo',
        scope: ['openid', 'profile', 'email'],
        access_type: undefined,
        // access_type: 'offline',
        access_token_endpoint: 'http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/token',
        // response_type: 'token',
        response_type: 'code',
        token_type: 'Bearer',
        // redirect_uri: 'http://localhost:3000/jobs',
        client_id: 'job-frontend',
        token_key: 'access_token',
        state: 'UNIQUE_AND_NON_GUESSABLE'
      },
      redirect: {
        login: '/jobs',
        callback: '/callback',
        home: '/'
      },
    },
  },
  router: {
    middleware: ['auth']
  },
  publicRuntimeConfig: {
    axios: {
      browserBaseURL: process.env.BROWSER_BASE_URL
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
