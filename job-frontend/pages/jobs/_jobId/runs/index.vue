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
      <span class="nav-current">Runs</span>
    </nav>
    <article>
      <div>
        <h1>Runs</h1>
        <RunList :job-id="jobId" />
      </div>
    </article>
  </div>
</template>

<script>
import RunList from '~/components/RunList.vue';

export default {

  components: {
    RunList
  },
  middleware: 'authenticated',
  layout: 'auth',

  data() {
    return {
      jobId: this.$route.params.jobId,
      job: ''
    };
  },

  mounted() {
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
};
</script>

<style></style>
