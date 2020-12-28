
export const actions = {
  async changePassword(context, data) {
    try {
      let result = await this.$axios.$post("/profile/changepassword", data)

      if (result.responseCode === 0) {
        await context.dispatch("snackbar/showSuccess", "Password changed!", {root: true})
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      if (e.response.status && e.response.status === 401) {
        await context.dispatch("snackbar/showError", "Incorrect current password specified!", {root: true})
      } else {
        await context.dispatch("snackbar/showError", "Unknown error while changing the password!", {root: true})
      }
    }
  },
}


