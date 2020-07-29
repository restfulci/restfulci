<template>
  <div>
    <p
      v-for="(job, index) in jobs"
      :key="index"
    >
      <nuxt-link :to="'/jobs/' + job.id">
        {{ job.name }}
      </nuxt-link>
    </p>
  </div>
</template>

<script>
export default {

  data() {
    return {
      jobs: []
    };
  },

  mounted() {
    this.$axios.get('/jobs')
    .then(response => {
      this.jobs = response.data;
    });
  },
};
</script>
