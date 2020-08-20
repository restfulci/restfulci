<template>
  <div>
    <nav>
      <nuxt-link :to="'/'">
        Home
      </nuxt-link> &rarr;
      <nuxt-link :to="'/jobs'">
        Jobs
      </nuxt-link> &rarr;
      <span class="nav-current">{{ job.name }}</span>
    </nav>
    <article>
      <div>
        <h1>
          {{ job.name }}
          <form @submit.prevent="goSettings">
            <input
              type="submit"
              value="Settings"
            >
          </form>
        </h1>
        <p>Remote origin: {{ job.remoteOrigin }}</p>
        <p>Config filepath: {{ job.configFilepath }}</p>

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
        <RunList :job-id="jobId" />
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

  methods: {
    goSettings() {
      this.$router.push("/jobs/" + this.jobId + "/settings");
    },
  },
};
</script>

<style></style>
