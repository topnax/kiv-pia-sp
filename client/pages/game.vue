<template>
  <v-container>
    <board :squares="squares" :size="5"/>
  </v-container>
</template>



<script>
import {mapGetters, mapState} from "vuex";

export default {
  computed: {
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
    Cell: () => import('../components/game/Cell')
  }
}
</script>

<style scoped>

</style>
