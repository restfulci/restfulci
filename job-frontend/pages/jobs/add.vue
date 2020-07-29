<template>
  <div>
    <nav>
      <nuxt-link :to="'/'">
        Home
      </nuxt-link> &rarr;
      <nuxt-link :to="'/jobs'">
        Jobs
      </nuxt-link> &rarr;
      <span class="nav-current">Add</span>
    </nav>
    <article>
      <div>
        <form @submit.prevent="addGitJob">
          <table class="fill-in">
            <tr>
              <td>Name</td>
              <td>
                <input
                  id="name"
                  v-model="job.name"
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
              <td>Remote origin</td>
              <td>
                <input
                  id="remoteOrigin"
                  v-model="job.remoteOrigin"
                  type="text"
                  value=""
                >
                <span
                  v-if="errors.remoteOrigin"
                  class="error"
                >{{ errors.remoteOrigin }}</span>
              </td>
            </tr>
            <tr>
              <td>Config file path</td>
              <td>
                <input
                  id="configFilepath"
                  v-model="job.configFilepath"
                  type="text"
                  value=""
                >
                <span
                  v-if="errors.configFilepath"
                  class="error"
                >{{ errors.configFilepath }}</span>
              </td>
            </tr>
            <tr>
              <td />
              <td class="button">
                <input
                  type="submit"
                  value="Add git job"
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
      job: {
        name: '',
        remoteOrigin: '',
        configFilepath: ''
      },
      errors: {
        name: '',
        remoteOrigin: '',
        configFilepath: ''
      }
    };
  },

  methods: {
    addGitJob() {
      console.log("Add git job!!");
      this.$axios.post('/jobs', this.job,
      {
        headers: {
          "Content-Type": "application/json"
        }
      }).then((response) => {
          this.$router.push('/jobs/'+response.data.id);
        })
        .catch((error) => {
          /*
          TODO:
          If there's a way to reset to initial value, rather than define
          it as initial value again.
          */
          this.errors = {
            name: '',
            remoteOrigin: '',
            configFilepath: ''
          };
          /*
          TODO:
          This ties to Java Validation error output form. Should be more
          general/lightweighted.
          Also, it seems if I use Spring `ExceptionHandler` to wrap/customize
          other error messages (e.g. `ItemNotUniqueException`), it will
          override Java Validation error output.
          */
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
