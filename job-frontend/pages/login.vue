<template>
  <div>
    <article>
      <div class="login">
        <form @submit.prevent="login">
          <table class="fill-in">
            <tr>
              <td>Username</td>
              <td>
                <input
                  id="username"
                  v-model="username"
                  name="username"
                  type="text"
                  value=""
                >
              </td>
            </tr>
            <tr>
              <td>Password</td>
              <td>
                <input
                  id="password"
                  v-model="password"
                  name="password"
                  type="password"
                  value=""
                >
              </td>
            </tr>
            <tr v-if="errorMessage">
              <td />
              <td class="error">
                {{ errorMessage }}
              </td>
            </tr>
            <tr>
              <td />
              <td class="button">
                <input
                  type="submit"
                  value="Log in"
                >
                <input
                  type="button"
                  onclick="location.href='/register';"
                  value="Sign up"
                >
              </td>
            </tr>
          </table>
        </form>
      </div>
    </article>
  </div>
</template>

<script>
const Cookie = process.client ? require('js-cookie') : undefined;
export default {
  middleware: 'notAuthenticated',
  layout: 'unauth',
  data() {
    return {
      username: '',
      password: '',
      errorMessage: '',
    };
  },
  methods: {
    login() {
      console.log("Login user "+this.username+"!!");
      /*
       * TODO:
       * Currently facing error
       * > has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
       * Execute in CURL everything is fine.
       * /jobs/add behaves the same when backend is 8080 (local)
       * but is fine when it is 8881 (inside of docker-compose)
       */
      setTimeout(() => { // we simulate the async request with timeout.
        this.$axios.post(
          'http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/token',
          "client_id=job-microservice"
          + "&client_secret=3b9857f3-56bd-43a0-befd-427cc14b5350"
          + "&username="+self.username.value
          + "&password="+self.password.value
          + "&grant_type=password",
          {
            headers: {
              "Content-Type": "application/x-www-form-urlencoded",
              // "Access-Control-Allow-Origin": "http://localhost:8880",
              // "Access-Control-Allow-Origin": "*",
              // "Access-Control-Allow-Methods": "GET,HEAD,OPTIONS,POST,PUT",
              // "Access-Control-Allow-Headers": "Access-Control-Allow-Headers, Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers",
            },
            // auth: {
            //   username: 'gitenter-envuelope',
            //   password: 'secretpassword'
            // },
            // withCredentials: true,
            // crossDomain: true,
          }
        ).then((response) => {
            console.log(response);
            const auth = {
              username: this.username,
              accessToken: response.data.access_token
            };
            console.log(auth);
            this.$store.commit('setAuth', auth); // mutating to store for client rendering
            Cookie.set('auth', auth); // saving token in cookie for server rendering
            /*
             * TODO:
             * Redirect to where the access page is without an credential, rather than
             * always the front page.
             */
            this.$router.push('/');
          })
          .catch((error) => {
            this.username = '';
            this.password = '';
            this.errorMessage = 'Invalid username and password!';
            console.log("here is the error");
            console.log(error);
          });
      }, 1000);
    }
  }
};
</script>

<style></style>
