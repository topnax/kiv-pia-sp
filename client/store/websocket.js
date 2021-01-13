import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex);

export const state = () => ({
  socket: {
    isConnected: false,
    message: '',
    reconnectError: false,
  },
  tokenSent: false
})

export const mutations = {

  SOCKET_ONOPEN (state, event) {
    console.log(">>>>>>Socket open")
    Vue.prototype.$socket = event.currentTarget
    state.socket.isConnected = true
    console.log(`tokenSent ${state.tokenSent}`)
    let token = this.$auth.strategy.token.get()

    console.log(token)
    if (token !== false && state.tokenSent === false && token !== undefined) {
      token = token.substring(7, token.length)
      console.log(`Sending token=${token}`)
      Vue.prototype.$socket.send("jwt;" + token)
      state.tokenSent = true
    }
  },

  SEND_TOKEN(state) {
    if (state.socket.isConnected) {
      console.log(`tokenSent ${state.tokenSent}`)
      let token = this.$auth.strategy.token.get()
      if (token !== false && state.tokenSent === false && token !== undefined) {
        token = token.substring(7, token.length)
        console.log(`Sending token=${token}`)
        Vue.prototype.$socket.send("jwt;" + token)
        state.tokenSent = true
      }
    }
  },

  RESET_TOKEN(state) {
    state.tokenSent = false
  },

  SOCKET_ONCLOSE (state, event) {
    console.log(">>>>>>Socket close")
    state.socket.isConnected = false
  },
  SOCKET_ONERROR (state, event) {
    console.log(">>>>>>Socket error")
    console.error(state, event)
  },
// default handler called for all methods
  SOCKET_ONMESSAGE (state, message) {
    console.log(">>>>>>message received: " + message.data)
  },
// mutations for reconnect methods
  SOCKET_RECONNECT(state, count) {
    console.log(">>>>>>Socket reconnect")
    console.info(state, count)
  },
  SOCKET_RECONNECT_ERROR(state) {
    console.log(">>>>>>Socket reconnect error")
    state.socket.reconnectError = true;
  },
}

export const actions = {
  sendMessage: function(context, message) {
    Vue.prototype.$socket.send(message)
  },
  sendToken(context) {
    context.commit("SEND_TOKEN")
  },
  resetToken(context) {
    context.commit("RESET_TOKEN")
  }
}
