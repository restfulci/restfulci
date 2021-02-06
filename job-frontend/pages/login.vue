<template>
  <div>
    <h3>Login</h3>
    <button @click="login()">Login via Keycloak</button>
  </div>
</template>

<script>
export default {
  layout: 'unauth',

  methods: {
    login() {
      this.$auth.loginWith("keycloak");
    }
  }
};
</script>
