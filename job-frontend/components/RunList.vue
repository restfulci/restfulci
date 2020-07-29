<template>
  <div>
    <p
      v-for="(run, index) in runs"
      :key="index"
    >
      <nuxt-link :to="'/jobs/' + jobId + '/runs/' + run.id">
        {{ run.id }}
      </nuxt-link>
    </p>
  </div>
</template>

<script>
export default {
  props: {
    jobId: {
      type: String,
      default: '0'
    }
  },

  data() {
    return {
      runs: []
    };
  },

  mounted() {
    this.$axios.get('/jobs/'+this.jobId+"/runs")
    .then(response => {
      this.runs = response.data;
    });
  },
};
</script>
