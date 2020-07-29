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
      <span class="nav-current">Trigger new run</span>
    </nav>
    <article>
      <form @submit.prevent="triggerNewRun">
        <table class="fill-in">
          <tr
            v-for="(parameter, index) in job.parameters"
            :key="index"
          >
            <td>{{ parameter.name }}<span v-if="parameter.isRequired">*</span></td>
            <td>
              <input
                v-model="parameter.value"
                type="text"
                value=""
              >
            </td>
          </tr>
          <tr>
            <td />
            <td class="button">
              <input
                type="submit"
                value="Trigger new run"
              >
            </td>
          </tr>
        </table>
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
    this.$axios.get('/jobs/'+this.jobId)
    .then(response => {
      this.job = response.data;
      if (this.job['type'] === 'GIT') {
        this.job.parameters.unshift({'name': 'commitSha', 'isRequired': false});
        this.job.parameters.unshift({'name': 'branchName', 'isRequired': false});
      }
    });
  },

  methods: {
    triggerNewRun() {
      var input = {};
      var i;
      for (i = 0; i < this.job.parameters.length; ++i) {
        var parameter = this.job.parameters[i];
        if (parameter['value']) {
          input[parameter['name']] = parameter['value'];
        }
      }
      this.$axios.post('/jobs/'+this.job.id+'/runs', input,
      {
        headers: {
          "Content-Type": "application/json"
        }
      }).then((response) => {
          this.$router.push('/jobs/'+this.job.id+'/runs/'+response.data.id);
        });
    }
  }
};
</script>

<style></style>
