<template>
  <v-container>
    <v-row>
      <v-col align="center">
        <v-btn @click="onCloseClicked">CLOSE</v-btn>
      </v-col>
    </v-row>

    <v-row>

      <v-col align="center" md="3" >
        <v-btn icon @click="back">
          <v-icon>mdi-arrow-left</v-icon>
        </v-btn>
        Turn #{{ currentTurn + 1 }}
        <v-btn icon @click="forward">
          <v-icon>mdi-arrow-right</v-icon>
        </v-btn>
        <v-progress-linear
          v-if="loading"
          indeterminate
          color="teal"/>
          <game-header v-if="!loading" :noughtsTurn="currentTurn % 2 === 0" :crossTurn="currentTurn % 2 !== 0" :finished="isFinished"
                       :noughtSeedUsername="game.noughtUsername" :crossSeedUsername="game.crossUsername" :winner="game.winner"/>
          <board v-if="!loading" :squares="getCells" :size="boardSize" :victoriousCells="getVictoriousCells"/>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
export default {
  async mounted() {
    await this.$store.dispatch("history/fetchTurns")
    console.log("mounted!")
    console.log(this.$props.turns)
  },
  name: "GameReplay",
  components: {
    Board: () => import('../components/game/Board'),
    GameHeader: () => import('../components/game/GameHeader'),
  },
  props: {
    onCloseClicked: null,
    loading: true,
    boardSize: Number,
    turns: null,
    game: {}
  },
  data: () => ({
    cells: Array(25).fill(null),
    victoriousCells: [],
    currentTurn: -1
  }),
  methods: {
    forward() {
      let turns = this.$props.turns
      if (turns !== null) {

        console.log("fd")
        if (this.$data.currentTurn < turns.length - 1) {
          console.log("fwd")
          console.log(turns.length)
          this.$data.currentTurn++
        }
      }
    },
    back() {
      let turns = this.$props.turns
      if (turns !== null) {
        if (this.$data.currentTurn >= 0) this.$data.currentTurn--
      }
    },
  },
  computed: {
    getCells() {
      let boardSize = this.$props.boardSize
      let cells = Array(boardSize * boardSize)
      if (this.currentTurn === -1) {
        return cells
      }

      this.$props.turns.slice(0, this.currentTurn + 1).forEach(
        turn => {
          console.log("")
          let index = indexByRow(turn.column, turn.row, boardSize)
          console.log(`setting index=${index} to ${turn.seed}`)
          cells[index] = turn.seed
        }
      )
      return cells

    },
    getVictoriousCells() {
      let boardSize = this.$props.boardSize
      let cells = Array(boardSize * boardSize)
      if (this.$data.currentTurn !== this.$props.turns.length - 1) {
        return cells
      }

      this.$props.turns.filter(turn => turn.victorious).forEach(
        turn => {
          let index = indexByRow(turn.column, turn.row, boardSize)
          cells[index] = turn.seed
        }
      )
      return cells

    },
    isFinished() {
      if (this.$props.turns === null) return false
      console.log(`cu ${this.$data.currentTurn} vs ${this.$props.turns.length}`)
      return this.$data.currentTurn === this.$props.turns.length - 1
    }
  }
}

function indexByRow(column, row, max = 3) {
  let index = row * max + column
  return ((row * max) + column)
}
</script>


