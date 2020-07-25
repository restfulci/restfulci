<template>
  <div>
    <nav>
      <nuxt-link :to="'/'">Home</nuxt-link> &rarr;
      <nuxt-link :to="'/jobs'">Jobs</nuxt-link> &rarr;
      <nuxt-link :to="'/jobs/' + jobId">{{ job.name }}</nuxt-link> &rarr;
      <nuxt-link :to="'/jobs/' + jobId + '/runs'">Runs</nuxt-link> &rarr;
      <span class="nav-current">{{ runId }}</span>
    </nav>
    <article>
    </article>
  </div>
</template>

<script>
export default {
  layout: 'auth',

  data() {
    return {
      jobId: this.$route.params.jobId,
      runId: this.$route.params.runId,
      run: '',
      job: '',
    };
  },

  mounted() {
    this.$axios.get('/jobs/'+this.jobId + '/runs/' + this.runId)
    .then(response => {
      this.run = response.data;
      this.job = this.run.job
    });
  },
};
</script>

<style></style>
