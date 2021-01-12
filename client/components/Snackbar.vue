<template>
  <v-container bottom="1200">
    <v-row>

      <v-snackbar
        v-for="(snackbar, index) in snackbars.filter(s => s.showing)"
        :key="snackbar.text + Math.random()"
        v-model="snackbar.showing"
        :timeout="snackbar.timeout"
        :color="snackbar.color"
      >
        <v-container>
          <v-row align="center">
            {{ snackbar.text }}
            <v-spacer/>
            <v-btn
              color="white"
              text
              @click="snackbar.showing = false"
              align-end
            >
              Close
            </v-btn>
          </v-row>
        </v-container>
      </v-snackbar>
    </v-row>

  </v-container>
</template>

<script>
export default {
name: "Snackbar",
  props: {
    snackbars: Array
  }
}
</script>

<style scoped>

</style>
