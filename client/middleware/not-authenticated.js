export default async function ({ next, store, $auth }) {
  let user = $auth.$state.user
  if (user) {
    return next('/dashboard')
  }
}
