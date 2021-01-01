export const state = () => ({
  onlineUsers: [],
  friends: [],
})

export const mutations = {
  SET_ONLINE_USERS(state, onlineUsers) {
    state.onlineUsers = onlineUsers
    console.log(state.onlineUsers)
  },
  ADD_ONLINE_USER(state, user) {
    console.log("adding")
    state.onlineUsers.push(user)
    console.log(state.onlineUsers)
  },
  REMOVE_ONLINE_USER(state, user) {
    console.log("user offline")
    console.log(user)
    console.log(state.onlineUsers)
    const index = state.onlineUsers.indexOf(user)
    const i = state.onlineUsers.map(user => user.id).indexOf(user.id);
    if (i > -1) {
      console.log("removing")
      console.log(user)
      state.onlineUsers.splice(i, 1);
    }
  }
}

export const actions = {
  async userOnline(context, data) {
    await context.commit("ADD_ONLINE_USER", {username: data.username, id: data.id})
  },
  async offlineUser(context, data) {
    await context.commit("REMOVE_ONLINE_USER", {username: data.username, id: data.id})
  },
  async onlineUsers(context, data) {
    console.log("Online users!")
    await context.commit("SET_ONLINE_USERS", data.users)
  },

}

export const getters = {
  otherOnlineUsers: (state, getters, rootState, rootGetters) => {
    console.log(`auth.id=${rootState.auth.user.id}`)
    console.log(state.onlineUsers)
    console.log(rootState)
    return state.onlineUsers.filter(user => user.id !== rootState.auth.user.id)
  }
}
