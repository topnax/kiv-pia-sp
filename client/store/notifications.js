export const actions = {
  async new(context, notification) {
    await context.dispatch("snackbar/showInfo", notification.text, {root: true})
  },
}
