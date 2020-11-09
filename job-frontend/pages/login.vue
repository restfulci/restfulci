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
      setTimeout(() => { // we simulate the async request with timeout.
        this.$axios.post(
          process.env.authServer + "/auth/realms/restfulci/protocol/openid-connect/token",
          "client_id=job-microservice"
          + "&client_secret=" + process.env.authClientSecret
          + "&username="+self.username.value
          + "&password="+self.password.value
          + "&grant_type=password",
          {
            headers: {
              "Content-Type": "application/x-www-form-urlencoded",
            },
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
            this.$router.push('/jobs');
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
