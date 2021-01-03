import Vue from 'vue'

export const state = () => ({
  boardSize: Number,
  seed: String,
  squares: Array(25).fill(null),
  state: "NONE",

  pending: {
    id: Number,
    boardSize: 0,
    victoriousCells: 0,
    ownerSeed: String,
    opponentSeed: String,
    invitedUsers: [
      "Pavel",
      "Tomáš"
    ]

  },

  playing: {
    turns: Array,
    boardSize: Number,
    ownerSeed: String,
    opponentSeed: String,
    opponentUsername: String,
    winner: String,
    victoriousCells: Array
  }

})

export const mutations = {


  SET_NONE_STATE(state, pendingState) {
    state.state = "NONE"
  },

  SET_PENDING_STATE(state, pendingState) {
    if (state.state === "NONE" || state.state === "PENDING") {
      state.pending.id = pendingState.id
      state.pending.boardSize = pendingState.boardSize
      state.pending.victoriousCells = pendingState.victoriousCells
      state.state = "PENDING"
    } else {
      console.error("received pending while playing is set")
    }
  },
  SET_SQUARE(state, {index, seed}) {
    state.squares[index] = seed
    Vue.set(state.squares, index, seed)
    console.log(`at index=${index}`)
    console.log(state.squares[index])
  }
}

export const actions = {
  async leave(context, data) {
    try {
      let result = await this.$axios.$post("/game/leave")

      if (result.responseCode === 0) {
        await this.$router.push("/dashboard")
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      await context.dispatch("snackbar/showError", "Could not leave game", {root: true})
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
  async setState(context, gameState) {
    switch (gameState.stateType) {
      case "PENDING":
        context.commit("SET_PENDING_STATE", gameState.state)
        break;
      case "NONE":
        context.commit("SET_NONE_STATE")
        break;
      default:
        await context.dispatch("snackbar/showError", `Unknown game state ${gameState.stateType}`, {root: true})
        break;
    }
  }
}
