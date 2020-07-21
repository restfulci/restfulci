<template>
  <div>
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
      </div>
    </article>
  </div>
</template>

<script>
export default {
  layout: 'auth',

};
</script>

<style></style>
