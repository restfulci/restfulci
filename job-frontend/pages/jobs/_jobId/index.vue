<template>
  <div>
    <article>
      <div>
        <h1>{{ job.name }}</h1>
        <p>remote origin: {{ job.remoteOrigin }}</p>
        <p>config filepath: {{ job.configFilepath }}</p>

        <h3>
          <nuxt-link :to="'/jobs/' + job.id + '/parameters'">
            Config parameters
          </nuxt-link>
        </h3>

        <h3>
          <nuxt-link :to="'/jobs/' + job.id + '/trigger'">
            Trigger a run
          </nuxt-link>
        </h3>

        <h3>Runs</h3>
        <RunList v-bind:jobId="jobId" />

      </div>
    </article>
  </div>
</template>

<script>
import RunList from '~/components/RunList.vue';

export default {
  layout: 'auth',

  components: {
    RunList
  },

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
