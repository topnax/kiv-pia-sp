export const state = () => ({})

export const actions = {
  async register(context, data) {
    try {
      let result = await this.$axios.$post("/register", {
        email: data.email,
        username: data.username,
        password: data.password
      })
      if (result.responseCode === 0) {
        await context.dispatch("snackbar/showSuccess", "Logged in!", {root: true})
        if (data.login) {
          await context.dispatch("login", {email: data.email, password: data.password})
        }
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      console.log(e)
      await context.dispatch("snackbar/showError", "Did not register!", {root: true})
    }
  },

  async login(context, data) {
    try {
      await this.$auth.loginWith("local", {
        data
      })
      await context.dispatch("snackbar/showSuccess", "Logged in!", {root: true})
    } catch (e) {
      await context.dispatch("snackbar/showError", "Invalid credentials", {root: true})
    }
  }
}
