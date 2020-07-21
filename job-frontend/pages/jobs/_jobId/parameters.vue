<template>
  <div>
    <article>
      <div>
        <h1>Current parameters</h1>
        <p
          v-for="(parameter, index) in job.parameters"
          :key="index"
        >
          {{ parameter.name }}
        </p>
        <form @submit.prevent="addParameter">
          <table class="fill-in">
            <tr>
              <td>Name</td>
              <td>
                <input
                  id="name"
                  v-model="newParameter.name"
                  name="name"
                  type="text"
                  value=""
                >
                <span
                  v-if="errors.name"
                  class="error"
                >{{ errors.name }}</span>
              </td>
            </tr>
            <tr>
              <td />
              <td class="button">
                <input
                  type="submit"
                  value="Add parameter"
                >
              </td>
            </tr>
          </table>
        </form>
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
      job: '',
      newParameter: {
        name: ''
      },
      errors: {
        name: ''
      }
    };
  },

  mounted() {
    this.loadJob();
  },

  methods: {
    loadJob() {
      this.$axios.get('/jobs/'+this.jobId)
      .then(response => {
        console.log(response.data);
        this.job = response.data;
      });
    },

    addParameter() {
      this.$axios.post('/jobs/'+this.job.id+'/parameters', this.newParameter,
      {
        headers: {
          "Content-Type": "application/json"
        }
      }).then((response) => {
          console.log(response);
          this.loadJob();
        })
        .catch((error) => {
          this.errors = {
            name: ''
          };
          var attrError;
          for (attrError of error.response.data.errors) {
            this.errors[attrError['field']] = attrError['defaultMessage'];
          }
          console.log(this.errors);
        });
    }
  }
};
</script>

<style></style>
