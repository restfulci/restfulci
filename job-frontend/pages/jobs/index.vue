<template>
  <div>
    <nav>
      <nuxt-link :to="'/'">
        Home
      </nuxt-link> &rarr;
      <span class="nav-current">Jobs</span>
    </nav>
    <article>
      <div>
        <h1>
          Jobs

          <form
            id="command"
            action="/jobs/add"
            method="GET"
          >
            <input
              type="submit"
              value="+"
            >
          </form>
        </h1>

        <JobList />
      </div>
    </article>
  </div>
</template>

<script>
import JobList from '~/components/JobList.vue';

export default {

  components: {
    JobList
  },
  middleware: 'authenticated',
  layout: 'auth',
};
</script>

<style></style>
