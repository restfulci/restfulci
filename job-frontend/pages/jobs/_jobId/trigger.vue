<template>
  <div>
    <article>
      <div>
        <p
          v-for="(parameter, index) in job.parameters"
          :key="index"
        >
          {{ parameter.name }}
        </p>
      </div>
    </article>
  </div>
</template>

<script>
export default {
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
  }
};
</script>

<style></style>
