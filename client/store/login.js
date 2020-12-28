export const state = () => ({
})

export const actions = {
  async register(context, data) {
    try {
      let result = await this.$axios.$post("/register", data)
      if (result.responseCode === 0) {
        await context.dispatch("snackbar/showSuccess", "Logged in!", {root: true})
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      console.log(e)
      await context.dispatch("snackbar/showError", "Did not register!", {root: true})
    }
  },
}
