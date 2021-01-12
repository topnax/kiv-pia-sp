export default async function ({ next, store, $auth }) {
  let user = $auth.$state.user
  if (!user || !user.admin) {
    await store.dispatch("snackbar/showError", "Not authorized", {root: true})
    return next('/dashboard')
  }
}
