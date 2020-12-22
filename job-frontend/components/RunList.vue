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
    this.$axios.get(
      this.$config.apiServer + '/jobs/' + this.jobId + "/runs", {
        headers: {
          'Authorization': "Bearer " + this.$store.state.auth.accessToken
        }
      }
    )
    .then(response => {
      this.runs = response.data;
    });
  },
};
</script>
