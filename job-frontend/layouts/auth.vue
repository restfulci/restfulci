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
      Copyright &copy; 2020 RestfulCI {{ token }}
    </footer>
  </div>
</template>

<script>
const Cookie = process.client ? require('js-cookie') : undefined;
export default {
  data() {
    return {
      username: this.$auth.user,
      token: this.$auth.getToken('keycloak')
    };
  },
  methods: {
    logout() {
      this.$auth.logout(/* .... */);
    }
  }
};
</script>

<style></style>
