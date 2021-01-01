export const state = () => ({
  onlineUsers: [],
  friends: [],
})

export const mutations = {
  SET_ONLINE_USERS(state, onlineUsers) {
    let onlineUserIds = state.onlineUsers.map(user => user.id)
    onlineUsers.filter(
      user => onlineUserIds.indexOf(user.id) === -1
    ).forEach(
      user => state.onlineUsers.push(user)
    )
  },
  ADD_ONLINE_USER(state, user) {
    if (state.onlineUsers.map(user => user.id).indexOf(user.id) === -1) {
      state.onlineUsers.push(user)
    }
  },
  REMOVE_ONLINE_USER(state, user) {
    const i = state.onlineUsers.map(user => user.id).indexOf(user.id);
    if (i > -1) {
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
    await context.commit("SET_ONLINE_USERS", data.users)
  },

}

export const getters = {
  otherOnlineUsers: (state, getters, rootState, rootGetters) => {
    return state.onlineUsers.filter(user => user.id !== rootState.auth.user.id).filter(user => rootState.friends.friends.map(friend => friend.id).indexOf(user.id) === -1)
  }
}
