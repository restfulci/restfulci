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
      <p>Triggered by: {{ user.username }}</p>
      <p>Triggered at: {{ run.triggerAt }}</p>
      <p v-if="run.completeAt !== null">
        Completed at: {{ run.completeAt }}
      </p>
      <p v-if="run.durationInSecond !== null">
        Duration: {{ run.durationInSecond }} seconds
      </p>
      <div v-if="run.errorMessage" class="error-log">
        Error message:<br>
        {{ run.errorMessage }}
      </div>
      <!--
        TODO:
        Don't show console log if not exist in response payload.
        `v-if="run.exitCode"` doesn't work because 0 is also false.
      -->
      <p v-if="run.exitCode !== null">
        Exit code: {{ run.exitCode }}
      </p>
      <!--
        TODO:
        Make it a component, and pass in both exitCode and consoleLog
        into it.
      -->
      <div v-if="consoleLog" class="console-log">
        ConsoleLog: {{ consoleLog }}
      </div>
    </article>
  </div>
</template>

<script>
export default {
  middleware: 'authenticated',
  layout: 'auth',

  data() {
    return {
      jobId: this.$route.params.jobId,
      runId: this.$route.params.runId,
      run: '',
      job: '',
      user: '',
      consoleLog: '',
    };
  },

  mounted() {
    this.loadRun();
    this.loadConsoleLog();
  },

  /*
   * Looks like there's no easy way to periodically update from API on
   * the status in case of SSR. Life cycle hooks like `updated` is not
   * called for SSR case.
   */

  methods: {
    loadRun() {
      this.$axios.get(
        this.$config.apiServer + '/jobs/' + this.jobId + '/runs/' + this.runId, {
          headers: {
            'Authorization': "Bearer " + this.$store.state.auth.accessToken
          }
        }
      )
      .then(response => {
        this.run = response.data;
        this.job = this.run.job;
        this.user = this.run.user;
        console.log(this.run);
      });
    },

    loadConsoleLog() {
      this.$axios.get(
        this.$config.apiServer + '/jobs/' + this.jobId + '/runs/' + this.runId + "/console", {
          headers: {
            'Authorization': "Bearer " + this.$store.state.auth.accessToken
          }
        }
      )
      .then(response => {
        this.consoleLog = response.data;
      });
    },
  },
};
</script>

<style></style>
