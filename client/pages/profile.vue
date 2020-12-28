<template>
  <v-row>
    <v-col class="text-center">
      <img
        src="/v.png"
        alt="Vuetify.js"
        class="mb-5"
      >
      <blockquote class="blockquote">
        Logged in :)
      </blockquote>
    </v-col>
  </v-row>
</template>

<script>
export default {
  name: "profile",
  middleware: ["auth"]
}
</script>

<style scoped>

</style>
