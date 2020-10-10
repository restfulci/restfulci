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
  layout: 'auth',

  data() {
    return {
      jobId: this.$route.params.jobId,
      job: ''
    };
  },

  mounted() {
    this.$axios.get('/jobs/'+this.jobId)
    .then(response => {
      this.job = response.data;
    });
  },
};
</script>

<style></style>
