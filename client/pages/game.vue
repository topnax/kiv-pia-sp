<template>
  <v-container>
    <pending v-if="game.state === `PENDING`" :boardSize="game.pending.boardSize" :victoriousCells="game.pending.victoriousCells" :id="game.pending.id" :invitedUsers="game.pending.invitedUsers"/>
    <board v-else-if="game.state === `PLAYING`" :squares="squares" :size="5"/>
    <span v-else-if="game.state === `NONE`">Currently not present in any game</span>
    <span v-else>Invalid state</span>
  </v-container>
</template>



<script>
import {mapGetters, mapState} from "vuex";

export default {
  mounted() {
    this.$store.dispatch("game/loadState")
  },
  computed: {

    ...mapState(["game"]),
    ...mapState({
      squares: state => state.game.squares
    }),

  },
  layout: "user_loggedin",
  name: "game",
  middleware: ["auth"],
  components: {
    Game: () => import('../components/game/Game'),
    Board: () => import('../components/game/Board'),
    Pending: () => import('../components/game/Pending'),
    Cell: () => import('../components/game/Cell')
  }
}
</script>

<style scoped>

</style>
