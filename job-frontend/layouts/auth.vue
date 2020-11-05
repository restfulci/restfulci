<template>
  <div>
    <header>
      <a href="/"><img
        class="logo"
        src="/images/logo.png"
        alt="restfulci"
        height="60"
        width="200"
      ></a>

      <form @submit.prevent="logout">
        <input
          id="logout"
          type="submit"
          value="Log out"
        >
      </form>

      <div class="text">
        Logged in as {{ username }}
      </div>
    </header>

    <nuxt />

    <footer>
      Copyright &copy; 2020 RestfulCI
    </footer>
  </div>
</template>

<script>
const Cookie = process.client ? require('js-cookie') : undefined;
export default {
  data() {
    return {
      username: this.$store.state.auth.username
    };
  },
  methods: {
    logout() {
      Cookie.remove('auth');
      this.$store.commit('setAuth', null);
      this.$router.push('/login');
    }
  }
};
</script>

<style></style>
