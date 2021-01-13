export const state = () => ({})

export const actions = {
  async register(context, data) {
    try {
      let result = await this.$axios.$post("/register", {
        email: data.email,
        username: data.username,
        password: data.password,
        confirmPassword: data.confirmPassword
      })
      if (result.responseCode === 0) {
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
      let result = await this.$auth.loginWith("local", {
        data
      })
      await context.dispatch("snackbar/showSuccess", "Logged in!", {root: true})
    } catch (e) {
      if (e.response && e.response.status && e.response.status === 401){
        await context.dispatch("snackbar/showError", "Invalid credentials", {root: true})
      } else {
        await context.dispatch("snackbar/showError", "Failed to login into the service", {root: true})
      }
    }
  }
}
