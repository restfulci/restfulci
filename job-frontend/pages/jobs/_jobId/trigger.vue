<template>
  <div>
    <article>
      <form @submit.prevent="triggerARun">
        <table class="fill-in">
          <tr
            v-for="(parameter, index) in job.parameters"
            :key="index"
          >
            <td>{{ parameter.name }}</td>
            <td>
              <!-- <input
                id="parameter"
                v-model="job.name"
                name="parameter"
                type="text"
                value=""
              > -->
              <!-- <span
                v-if="errors.name"
                class="error"
              >{{ errors.name }}</span> -->
            </td>
          </tr>
          <tr>
            <td />
            <td class="button">
              <input
                type="submit"
                value="Trigger a run"
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
      parameters: {}
    };
  },

  mounted() {
    this.$axios.get('/jobs/'+this.jobId)
    .then(response => {
      this.job = response.data;
    });
  },

  methods: {
    triggerARun() {

    }
  }
};
</script>

<style></style>
