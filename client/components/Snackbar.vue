<template>
  <v-container>
    <v-row>
      <v-snackbar
        v-for="(snack, index) in snackbars.filter(s => s.showing)"
        :key="snack.text + Math.random()"
        :value="snack.showing"
        @input="hideSnack(snack.id)"
        timeout="2000"
        :color="snack.color"
        :style="`bottom: ${(index * 80) + 8}px ; top: auto;`"
      >
        <v-container>
          <v-row align="center">
            {{ snack.text }}
            <v-spacer/>
            <v-btn
              color="white"
              text
              @click="snack.showing = false"
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
  },
  methods: {
    hideSnack(id) {
      this.$store.dispatch("snackbar/removeSnackbar", id)
    }
  },
}
</script>
