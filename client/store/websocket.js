import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex);

export const state = () => ({
  socket: {
    isConnected: false,
    message: '',
    reconnectError: false
  },
})

export const mutations = {

  SOCKET_ONOPEN (state, event) {
    console.log(">>>>>>Socket open")
    Vue.prototype.$socket = event.currentTarget
    state.socket.isConnected = true
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
  }
}
