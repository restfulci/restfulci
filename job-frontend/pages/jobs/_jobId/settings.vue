<template>
  <div>
    <nav>
      <nuxt-link :to="'/'">
        Home
      </nuxt-link> &rarr;
      <nuxt-link :to="'/jobs'">
        Jobs
      </nuxt-link> &rarr;
      <nuxt-link :to="'/jobs/' + jobId">
        {{ job.name }}
      </nuxt-link> &rarr;
      <span class="nav-current">Settings</span>
    </nav>
    <article>
      <form @submit.prevent="deleteJob">
        <input
          class="danger"
          type="submit"
          value="Delete job"
        >
      </form>
    </article>
  </div>
</template>

<script>
export default {
  layout: 'auth',

  data() {
    return {
      jobId: this.$route.params.jobId,
      job: '',
    };
  },

  mounted() {
    this.loadJob();
  },

  methods: {
    loadJob() {
      this.$axios.get(
        this.$config.apiServer + '/jobs/' + this.jobId, {
          headers: {
            'Authorization': "Bearer " + this.$store.state.auth.accessToken
          }
        }
      )
      .then(response => {
        this.job = response.data;
      });
    },
    deleteJob() {
      this.$axios.delete(
        this.$config.apiServer + '/jobs/' + this.jobId, {
          headers: {
            'Authorization': "Bearer " + this.$store.state.auth.accessToken
          }
        }
      )
      .then(response => {
        this.$router.push('/jobs');
        return response;
      });
    },
  }
};
</script>

<style></style>
