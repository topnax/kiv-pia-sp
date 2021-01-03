export default async function ({ next, store }) {
  if (store.state.game.state !== "NONE") {
    return next('/game')
  } else {
  }
}
