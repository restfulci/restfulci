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
      <nuxt-link :to="'/jobs/' + jobId + '/runs'">
        Runs
      </nuxt-link> &rarr;
      <span class="nav-current">{{ runId }}</span>
    </nav>
    <article>
      <h1 v-if="run.status == 'IN_PROGRESS'">
        ⌛
      </h1>
      <h1 v-if="run.status == 'SUCCEED'">
        ✅
      </h1>
      <h1 v-if="run.status == 'FAIL'">
        ❌
      </h1>
      <h1 v-if="run.status == 'ABORT'">
        󠁿⚪
      </h1>
      <p>Triggered at: {{ run.triggerAt }}</p>
      <p v-if="run.completeAt !== null">
        Completed at: {{ run.completeAt }}
      </p>
      <p v-if="run.exitCode !== null">
        Exit code: {{ run.exitCode }}
      </p>
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
      this.job = this.run.job;
      console.log(this.run);
    });
  },
};
</script>

<style></style>
