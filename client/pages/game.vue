<template>
  <v-container>
    <board :squares="['X', null, null, null, null, null, null, null, null,null, null, null]" :size="5"/>
  </v-container>
</template>



<script>
export default {
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
