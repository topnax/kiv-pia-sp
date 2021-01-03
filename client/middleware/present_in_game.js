export default async function ({ next, store }) {
  if (store.state.game.state !== "NONE") {
  } else {
    return next("/newgame")
  }
}
