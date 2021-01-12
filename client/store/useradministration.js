export const state = () => ({
  users: [],
  loading: false
})

export const mutations = {
  SET_USERS(state, users) {
    state.users = users
    console.log("Set users to:")
    console.log(users)
  },

  SET_LOADING(state, loading) {
    state.loading = loading
  },

}

export const actions = {

  async fetchUsers(context, data) {
    context.commit("SET_LOADING", true)
    await new Promise(resolve => setTimeout(resolve, 200))
    try {
      let result = await this.$axios.$get("/administration/users/list")

      if (result.responseCode === 0) {
        await context.commit("SET_USERS", result.data)
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      await context.dispatch("snackbar/showError", "Could not load users", {root: true})
    }

    context.commit("SET_LOADING", false)
  },

  async changePassword(context, data) {
    try {
      let result = await this.$axios.$post("/administration/users/changepassword", data)

      if (result.responseCode === 0) {
        await context.dispatch("snackbar/showSuccess", `User's password changed successfully`, {root: true})
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      await context.dispatch("snackbar/showError", "Could not change user's password", {root: true})
    }
  },

  async changeUserRole(context, data) {
    try {
      let result = await this.$axios.$post("/administration/users/" + (data.promote ? "promote" : "demote"), {userId: data.userId})

      if (result.responseCode === 0) {
        await context.dispatch("snackbar/showSuccess", `User's role changed successfully`, {root: true})
        await context.dispatch("fetchUsers")
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      await context.dispatch("snackbar/showError", "Could not change user's role", {root: true})
    }
  },
}
