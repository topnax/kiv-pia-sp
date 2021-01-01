import Vue from 'vue'
import VueNativeSock from 'vue-native-websocket'

export default ({ store }, inject) => {
  Vue.use(VueNativeSock, 'ws://localhost:8080/api/ws', {
    store,
    format: 'json',
    namespaced: true,
    reconnection: true,
    reconnectionAttempts: 5,
    reconnectionDelay: 3000,
    passToStoreHandler: function (eventName, event) {
      console.log(`Got eventname ${eventName}`)
      console.log(`Got event ${event}`)
      console.log(event)
      if (!eventName.startsWith('SOCKET_')) { return }
      let method = 'commit'
      let target = eventName.toUpperCase()
      let msg = event
      if (this.format === 'json' && event.data) {
        msg = JSON.parse(event.data)
        console.log("what")
        console.log(msg)
        if (msg.mutation) {
          console.log("mutating")
          target = [msg.namespace || '', msg.mutation].filter((e) => !!e).join('/')
        } else if (msg.action) {
          console.log("lol")
          method = 'dispatch'
          target = [msg.namespace || '', msg.action].filter((e) => !!e).join('/')

          this.store[method](target, msg.data)
          return
        } else {
          target = (msg.namespace + "/" || '/') + target
        }
      } else {
        target = "websocket/" + target
      }
      // Write this if multiple stores are used and namespace options are enabled
      console.log(target)

      console.log(target)
      this.store[method](target, msg)
    }
  })
}
