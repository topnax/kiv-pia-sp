<template>
  <v-container
    class="px-0"
    fluid
    >
  <v-row justify="center">
    <v-col>
      <history-table v-if="history.game === null" :games="gamesParsed" :loading="history.loading" :onRowClick="onRowClick"/>
      <game-replay v-else :game="history.game" :onCloseClicked="onReplayClose" :boardSize="history.game.boardSize" :loading="history.turnsLoading" :turns="history.gameTurns"/>
    </v-col>
  </v-row>
  </v-container>
</template>

<script>
import {mapGetters, mapState} from "vuex";

export default {
  components: {
    HistoryTable: () => import('../components/HistoryTable'),
    GameReplay: () => import('../components/GameReplay'),
  },
  mounted() {
    this.$store.dispatch("history/fetchHistory")
  },
  methods: {
    onRowClick(row) {
      console.log("inside hiustory")
      alert(`row #${row.id} clicked`)
      console.log(row)
      this.$store.dispatch("history/setGame", row)
    },
    onReplayClose() {
      this.$store.dispatch("history/setGame", null)
    }
  },
  computed: {
    ...mapState(["history"]),
    ...mapGetters({
      gamesParsed: 'history/gamesParsed',
    }),
  },
  layout: "user_loggedin",
  name: "history",
  middleware: ["auth"]
}
</script>
