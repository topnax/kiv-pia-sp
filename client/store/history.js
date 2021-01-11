export const state = () => ({
  loading: false,
  games: []
})

export const mutations = {
  SET_GAMES(state, games) {
    state.games = games
  },
  SET_LOADING(state, loading) {
    state.loading = loading
  },
}

export const actions = {
  async fetchHistory(context, data) {
    context.commit("SET_LOADING", true)
    try {
      let result = await this.$axios.$get("/game/history")

      if (result.responseCode === 0) {
        await context.commit("SET_GAMES", result.data)
        console.log("got result:data")
        console.log(result.data)
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      await context.dispatch("snackbar/showError", "Could not load game history", {root: true})
    }
    context.commit("SET_LOADING", false)
  },
}

export const getters = {

  gamesParsed: (state, getters, rootState, rootGetters) => {
    console.log("state:")
    console.log(rootState.auth.user.username)
    return state.games.map(
      game => {
        console.log("inside getter")
        console.log(game.crossWon)
        game.winner = game.crossWon ? "X" : "O"
        let username = rootState.auth.user.username
        game.opponentUsername = game.crossUsername === username ? game.noughtUsername : game.crossUsername
        game.won = game.crossWon ? (game.crossUsername === username) : (game.noughtUsername === username)
        return game
      }
    )
  }
}
