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

  ADD_FRIEND(state, friend) {
    console.log("adding friend:")
    console.log(friend)
    state.friends.push(friend)
  },

  REMOVE_FRIEND(state, friend) {
    console.log("removing a friend:")
    console.log(friend)
    const i = state.friends.map(user => user.id).indexOf(friend.id);
    if (i > -1) {
      console.log("removing")
      console.log(friend)
      state.friends.splice(i, 1);
    }
  },

  SET_LOADING(state, loading) {
    state.loading = loading
  },

}

export const actions = {

  async newFriend(context, user) {
    await context.dispatch("snackbar/showSuccess", `${user.username} has been added to your friend list`, {root: true})
    await context.commit("ADD_FRIEND", user)
  },

  async friendGone(context, user) {
    await context.commit("REMOVE_FRIEND", user)
    await context.dispatch("snackbar/showInfo", `${user.username} has removed you from their friend list`, {root: true})
  },

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

export const getters = {
  onlineFriends: (state, getters, rootState, rootGetters) => {
    return rootState.users.onlineUsers
      .filter(user => user.id !== rootState.auth.user.id) // exclude current user
      .filter(user => rootState.friends.friends.map(friend => friend.id).indexOf(user.id) !== -1) // filter friends
  },
  offlineFriends: (state, getters, rootState, rootGetters) => {
    return state.friends
      .filter(friend => rootState.users.onlineUsers.map(user => user.id).indexOf(friend.id) === -1)
  }
}
