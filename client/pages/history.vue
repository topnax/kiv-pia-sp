<template>
  <v-row>
    <v-col>
      <v-data-table
        :headers="headers"
        :items="gamesParsed"
        :items-per-page="5"
        :loading="history.loading"
        loading-text="Loading... Please wait"
        class="elevation-1"
      >
        <template v-slot:item.won="{ item }">
          <v-icon v-if="item.won" color="yellow">
            mdi-trophy-variant
          </v-icon>
          <v-icon v-else color="error">
            mdi-cancel
          </v-icon>
        </template>
        <template v-slot:item.winner="{ item }">
          <v-icon v-if="item.winner === 'X'">
            mdi-close
          </v-icon>
          <v-icon v-else>
            mdi-circle-outline
          </v-icon>
        </template>
      </v-data-table>
    </v-col>
  </v-row>
</template>

<script>
import {mapGetters, mapState} from "vuex";

export default {
  mounted() {
    this.$store.dispatch("history/fetchHistory")
  },
  computed: {
    ...mapState(["history"]),
    ...mapGetters({
      gamesParsed: 'history/gamesParsed',
    }),
  },
  data: () => ({
    headers: [
      {text: 'Won', value: 'won'},
      {text: 'Opponent username', value: 'opponentUsername'},
      {text: 'Winner', value: 'winner'},
      {text: 'Board size', value: 'boardSize'},
      {
        text: 'Date',
        align: 'start',
        value: 'dateCreated',
      },
    ],
  }),
  layout: "user_loggedin",
  name: "history",
  middleware: ["auth"]
}
</script>
