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
        Logged in as {{ user.preferred_username }}
      </div>
    </header>

    <nuxt />

    <footer>
      Copyright &copy; 2020 RestfulCI
    </footer>
  </div>
</template>

<script>
export default {
  data() {
    return {
      user: this.$auth.user,
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
