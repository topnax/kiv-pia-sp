export const state = () => ({
  loading: false,
  games: [],
  game: null,
  gameTurns: null,
  turnsLoading: true,
})

export const mutations = {
  SET_GAMES(state, games) {
    state.games = games
  },
  SET_LOADING(state, loading) {
    state.loading = loading
  },
  SET_GAME(state, game) {
    state.game = game
  },
  SET_TURNS(state, turns) {
    state.gameTurns = turns
  },
  SET_TURNS_LOADING(state, loading) {
    state.turnsLoading = loading
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
  async fetchTurns(context, data) {
    context.commit("SET_TURNS_LOADING", true)
    console.log("about to fetch turns + " + context.state.game.id)
    try {
      let result = await this.$axios.$get("/game/history/turns/" + context.state.game.id)

      if (result.responseCode === 0) {
        context.commit("SET_TURNS", result.data)
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      await context.dispatch("snackbar/showError", "Could not load game turns", {root: true})
    }
    context.commit("SET_TURNS_LOADING", false)
  },
  setGame(context, game) {
    context.commit("SET_GAME", game)
  }
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
