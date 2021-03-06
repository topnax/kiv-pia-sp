export const state = () => ({
  requests: [],
  loading: false
})

export const mutations = {
  SET_REQUESTS(state, requests) {
    state.requests = requests
  },

  ADD_REQUEST(state, request) {
    state.requests.push(request)
  },

  SET_LOADING(state, loading) {
    state.loading = loading
  }
}

export const actions = {
  async acceptRequest(context, requestId) {
    context.commit("SET_LOADING", true)
    await new Promise(resolve => setTimeout(resolve, 200))
    try {
      let result = await this.$axios.$post("/friend/acceptRequest", {
        requestId
      })

      if (result.responseCode === 0) {
        await context.dispatch("fetchRequests")
        await context.dispatch("snackbar/showSuccess", "Friend request accepted!", {root: true})
      } else {

        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      await context.dispatch("snackbar/showError", "Could not load friend requests!", {root: true})
    }

    context.commit("SET_LOADING", false)
  },
  async declineRequest(context, requestId) {
    context.commit("SET_LOADING", true)
    await new Promise(resolve => setTimeout(resolve, 200))
    try {
      let result = await this.$axios.$post("/friend/declineRequest", {
        requestId
      })

      if (result.responseCode === 0) {
        await context.dispatch("fetchRequests")
        await context.dispatch("snackbar/showSuccess", "Friend request cancelled!", {root: true})
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      await context.dispatch("snackbar/showError", "Could not load friend requests!", {root: true})
    }

    context.commit("SET_LOADING", false)
  },

  async fetchRequests(context, data) {
    context.commit("SET_LOADING", true)
    console.log("about to wait")
    await new Promise(resolve => setTimeout(resolve, 200))
    console.log("have waited")
    try {
      let result = await this.$axios.$get("/friend/requests")

      if (result.responseCode === 0) {
        await context.commit("SET_REQUESTS", result.data)
      } else {

        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      await context.dispatch("snackbar/showError", "Could not load friend requests!", {root: true})
    }

    context.commit("SET_LOADING", false)
  },

  async newRequest(context, userId) {
    try {
      let result = await this.$axios.$post("/friend/new", {
        userId: userId
      })

      if (result.responseCode === 0) {
        // await context.dispatch("fetchRequests")
        await context.dispatch("snackbar/showSuccess", "Friend request sent!", {root: true})
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      await context.dispatch("snackbar/showError", "Could not send a new friend request", {root: true})
    }
  },

  async incomingRequest(context, data) {
    let request = data.request
    console.log("incoming request")
    console.log(data)
    await context.commit("ADD_REQUEST", request)
    await context.dispatch("snackbar/showInfo", `New friend request from ${request.requestorUsername}!`, {root: true})
  },

  async addRequest(context, request) {
    await context.commit("ADD_REQUEST", request)
    await context.dispatch("snackbar/showInfo", "Friend request sent!", {root: true})
  }
}
