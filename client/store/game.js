import Vue from 'vue'

export const state = () => ({
  boardSize: Number,
  seed: String,
  squares: Array(25).fill(null),
  state: "NONE",
  draw: false,
  finished: false,
  won: false,

  pending: {
    owner: Boolean,
    opponentUsername: String,

    id: Number,
    boardSize: 0,
    victoriousCells: 0,
    ownerSeed: String,
    opponentSeed: String,
    invitedUsers: []

  },

  playing: {
    turns: Array,
    boardSize: Number,
    ownerSeed: String,
    opponentSeed: String,
    playerSeed: String,
    victoriousCells: Array,
    opponentUsername: String,
    winner: String,
  },
  in_game: false
})

export const mutations = {


  SET_NONE_STATE(state, pendingState) {
    state.state = "NONE"
    state.pending = undefined
    state.playing = undefined
  },

  SET_PENDING_STATE(state, pendingState) {
    if (state.state === "NONE" || state.state === "PENDING") {
      state.pending = {}

      state.pending.opponentUsername = pendingState.opponentUsername
      state.pending.id = pendingState.id
      state.pending.owner = pendingState.owner
      state.pending.boardSize = pendingState.boardSize
      state.pending.victoriousCells = pendingState.victoriousCells
      state.pending.invitedUsers = pendingState.invitedUsers
      state.state = "PENDING"
      state.playing = {}
    } else {
      console.error("received pending while playing is set")
    }
  },
  SET_SQUARE(state, {index, seed}) {
    state.squares[index] = seed
    Vue.set(state.squares, index, seed)
    console.log(`at index=${index}`)
    console.log(state.squares[index])
  },

  SET_GAME_STATE(state, gameState) {
    state.draw = false
    state.finished = false
    state.won = false
    state.in_game = true;
    state.playing.turns = gameState.turns
    console.log("received turns:")
    console.log(gameState.turns)
    state.playing.opponentSeed = gameState.opponentSeed
    state.playing.opponentUsername = gameState.opponentUsername
    state.playing.boardSize = gameState.boardSize
    state.playing.victoriousCells = gameState.victoriousCells
    Vue.set(state.playing.victoriousCells, gameState.victoriousCells)
    state.playing.playerSeed = gameState.playerSeed
  },

  ADD_TURN(state, turn) {
    state.playing.turns.push(turn)
    Vue.set(state.squares, state.squares.length - 1, turn)
    console.log("turn added:")
    console.log(turn)
  },
  SET_DRAW(state) {
    state.draw = true
    state.finished = true
  },


  FINISHED_GAME_CLOSE(state, data) {
    state.in_game = false
    state.finished = false
    state.won = false
    state.draw = false
  },

  SET_FINISHED(state, data) {
    state.finished = true
    state.draw = false
    state.won = data.won
    state.playing.victoriousCells = data.victoriousCells
    Vue.set(state.playing.victoriousCells, data.victoriousCells)
    console.log("setting victorious:")
    console.log(data.victoriousCells)
    state.playing.winner = data.winnerSeed
  }
}

export const actions = {
  async gameDraw(context) {
    await context.commit("SET_DRAW")
  },
  async refresh(context, data) {
    let result = await this.$axios.$post("/game/refresh")
    if (result.responseCode !== 0) {
      await context.dispatch("snackbar/showError", "Could not refresh state", {root: true})
    }
  },
  async move(context, data) {
    await context.commit("SET_SQUARE", data)
  },
  async loadState(context, data) {
    try {
      let result = await this.$axios.$get("/game/get")

      if (result.responseCode === 0) {
        let gameState = result.data
        await context.dispatch("setState", gameState)
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      console.log(e)
      await context.dispatch("snackbar/showError", "Could not load the game state", {root: true})
    }

  },
  async play(context, {row, column}) {
    try {
      let result = await this.$axios.$post("/game/play", {row: row, column: column})

      if (result.responseCode === 0) {
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      console.log(e)
      await context.dispatch("snackbar/showError", "Could not place the seed.", {root: true})
    }
  },
  async setState(context, gameState) {
    await context.commit("SET_GAME_STATE", gameState)
  },
  async newTurn(context, turn) {
    context.commit("ADD_TURN", turn)
  },

  async gameWon(context, data) {
    console.log("set finished")
    await context.commit("SET_FINISHED", data)
  },

  async finishedGameClose(context, data) {
    await context.commit("FINISHED_GAME_CLOSE")
  },
  async surrender(context) {
    try {
      let result = await this.$axios.$post("/game/leave")

      if (result.responseCode === 0) {
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      console.log(e)
      await context.dispatch("snackbar/showError", "Could not leave the game.", {root: true})
    }
  }
}

export const getters = {
  cells: (state, getters, rootState, rootGetters) => {

    let boardSize = state.playing.boardSize
    let cells = Array(state.playing.boardSize * state.playing.boardSize).fill(null)
    state.playing.turns.forEach(
      turn => {

        console.log(`setting index ${indexByRow(turn.column, turn.row, boardSize)} with ${turn.seed}`)

        cells[indexByRow(turn.column, turn.row, boardSize)] = turn.seed
      }
    )


    console.log("dsaadas")
    console.log(cells)
    return cells
  },
  victoriousCells: (state, getters, rootState, rootGetters) => {
    console.log("victirous getter")
    let boardSize = state.playing.boardSize
    let cells = Array(state.playing.boardSize * state.playing.boardSize).fill(false)
    state.playing.victoriousCells.forEach(
      turn => {
        cells[indexByRow(turn.column, turn.row, boardSize)] = true
      }
    )

    return cells
  },
  noughtSeedUsername: (state, getters, rootState, rootGetters) => {
    if (state.playing.opponentSeed === "O") return state.playing.opponentUsername
    else return rootState.auth.user.username
  },
  crossSeedUsername: (state, getters, rootState, rootGetters) => {
    if (state.playing.opponentSeed === "X") return state.playing.opponentUsername
    else return rootState.auth.user.username
  },
  noughtsTurn: (state, getters, rootState, rootGetters) => {
    return state.playing.turns.length % 2 !== 0
  },
  crossTurn: (state, getters, rootState, rootGetters) => {
    return state.playing.turns.length % 2 === 0
  },
  usersTurn: (state, getters, rootState, rootGetters) => {
    if (state.playing.opponentSeed === "X") return state.playing.turns.length % 2 !== 0
    if (state.playing.opponentSeed === "O") return state.playing.turns.length % 2 === 0
  },
  userWon: (state, getters, rootState, rootGetters) => {
    if (state.playing.opponentSeed === "X") return state.playing.winner === "O"
    if (state.playing.opponentSeed === "O") return state.playing.winner === "X"
  }

}

function indexByRow(column, row, max = 3) {
  let index = row * max + column
  return ((row * max) + column)
}
