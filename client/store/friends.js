export const state = () => ({
  friends: [],
  loading: false
})

export const mutations = {
  SET_FRIENDS(state, friends) {
    console.log("setting friends:")
    console.log(friends)
    state.friends = friends
  },

  SET_LOADING(state, loading) {
    state.loading = loading
  },

  // default handler called for all methods
  SOCKET_ONMESSAGE (state, message) {
    console.log(">>>>>>friends received: " + message)
    console.log(message)
  },
}

export const actions = {
  async fetchFriends(context, data) {
    context.commit("SET_LOADING", true)
    await new Promise(resolve => setTimeout(resolve, 200))
    try {
      let result = await this.$axios.$get("/friend/list")

      if (result.responseCode === 0) {
        await context.commit("SET_FRIENDS", result.data)
        console.log("loaded friends:)")
        console.log(result.data)
      } else {

        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      await context.dispatch("snackbar/showError", "Could not load friend list!", {root: true})
    }

    context.commit("SET_LOADING", false)
  },

  async cancel(context, userId) {
    try {
      let result = await this.$axios.$post("/friend/cancel", {
        userId
      })

      if (result.responseCode === 0) {
        await context.dispatch("fetchFriends")
        await context.dispatch("snackbar/showSuccess", "Friendship ruined :(", {root: true})
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      await context.dispatch("snackbar/showError", "Could not cancel friendship. Please consider staying friends.", {root: true})
    }
  },
}
