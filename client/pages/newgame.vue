<template>
  <v-container
    class="px-0"
    fluid
  >
    <v-col md="3" class="mx-auto">
      <v-card>
        <v-card-title>Create a new game</v-card-title>
        <v-card-text>
          <span class="subtitle-1">Board size:</span>
          <v-radio-group v-model="radioGroup">
            <v-radio
              v-for="n in boardSizes"
              :key="n"
              :label="`${n} x ${n}`"
              :value="n"
            ></v-radio>
          </v-radio-group>
          <span class="subtitle-1">Winning squares:</span>
          <v-radio-group v-model="radioGroup">
            <v-radio
              v-for="n in boardSizes"
              :key="n"
              :label="`${n} x ${n}`"
              :value="n"
            ></v-radio>
          </v-radio-group>
        </v-card-text>

      </v-card>
    </v-col>
  </v-container>
</template>

<script>
export default {
  layout: "user_loggedin",
  name: "profile",
  data: () => ({
    radioGroup: 1,
    boardSizes: [3, 5, 10]
  }),
  middleware: ["auth"]
}
</script>

<style scoped>

</style>
