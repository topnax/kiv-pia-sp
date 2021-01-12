<template>
  <v-row>
    <v-col class="text-center">
      <img
        src="/v.png"
        alt="Vuetify.js"
        class="mb-5"
      >
      <blockquote class="blockquote">
        Welcome to a Tic Tac Toe game made as a <a href="https://github.com/osvetlik/pia2020/tree/master/semester-project">KIV/PIA semester project</a>.
      </blockquote>
    </v-col>
  </v-row>
</template>

<script>
export default {
  layout: "user_loggedin",
  name: "profile",
  middleware: ["auth"]
}
</script>

<style scoped>

</style>
