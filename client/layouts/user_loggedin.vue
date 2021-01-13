<template>
  <v-app dark>
    <v-navigation-drawer
      v-model="drawer"
      :clipped="clipped"
      fixed
      app
    >
      <v-list>

        <v-list-item
          v-for="(item, i) in items"
          :key="i"
          :to="item.to"
          router
          exact
        >
          <v-list-item-action>
            <v-icon>{{ item.icon }}</v-icon>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title v-text="item.title"/>
          </v-list-item-content>
        </v-list-item>

        <v-list-item
          :key="99"
          @click.stop="logout"
          exact
        >
          <v-list-item-action>
            <v-icon>mdi-logout</v-icon>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title>Logout</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        <div v-if="$store.$auth.user.admin">
          <hr>
          <v-list-item
            :key="100"
            to="/user-administration"
            exact
          >
            <v-list-item-action>
              <v-icon>mdi-account-group</v-icon>
            </v-list-item-action>
            <v-list-item-content>
              <v-list-item-title>User administration</v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </div>
      </v-list>
    </v-navigation-drawer>
    <v-app-bar
      :clipped-left="clipped"
      :clipped-right="clipped"
      fixed
      app
    >
      <v-app-bar-nav-icon @click.stop="drawer = !drawer"/>

      <v-toolbar-title v-text="title"/>
      <v-spacer/>
      <v-icon>mdi-account</v-icon>
      <span class="ms-3">{{ $store.$auth.user.username }} ({{ $store.$auth.user.email }})</span>
      <v-btn
        icon
        @click.stop="rightDrawer = !rightDrawer"
      >
        <v-icon v-if="rightDrawer">mdi-chevron-right</v-icon>
        <v-icon v-else>mdi-account-multiple</v-icon>
      </v-btn>
    </v-app-bar>
    <v-main>
      <v-container fill-height>
        <nuxt/>
        <snackbar :snackbars="snackbar.snackbars"/>
      </v-container>
    </v-main>
    <v-navigation-drawer
      v-model="rightDrawer"
      :right="right"
      :clipped="clipped"
      fixed
      app
    >
      <v-list class="ps-2 pt-5">

        <span class="title ms-2 mt-10 pt-10" v-if="friends.friends.length > 0">Friends:</span>

        <v-menu
          v-for="(item, i) in onlineFriends"
          transition="slide-x-transition"
          :close-on-click="true"
          bottom
          right
        >
          <template v-slot:activator="{ on, attrs }">
            <v-list-item
              dark
              v-bind="attrs"
              v-on="on"
              exact
            >
              <v-list-item-action>
                <v-icon color="green" size="10">mdi-checkbox-blank-circle</v-icon>
              </v-list-item-action>
              <v-list-item-content>
                <v-list-item-title v-text="item.username"/>
              </v-list-item-content>
            </v-list-item>
          </template>

          <v-list>
            <v-list-item
              :key="1"
              @click="cancelFriendship(item.id)"
            >
              <v-list-item-title>Remove from friend list</v-list-item-title>
            </v-list-item>

            <v-list-item
              v-if="game.pending !== undefined && game.pending.owner"
              :key="2"
              @click="inviteToGame(item.id)"
            >
              <v-list-item-title>Invite to a game</v-list-item-title>

            </v-list-item>
          </v-list>
        </v-menu>

        <v-menu
          v-for="(item, i) in offlineFriends"
          transition="slide-x-transition"
          :close-on-click="true"
          bottom
          right
        >
          <template v-slot:activator="{ on, attrs }">
            <v-list-item
              dark
              v-bind="attrs"
              v-on="on"
              exact
            >
              <v-list-item-action>
                <v-icon color="red" size="10">mdi-checkbox-blank-circle</v-icon>
              </v-list-item-action>
              <v-list-item-content>
                <v-list-item-title v-text="item.username"/>
              </v-list-item-content>
            </v-list-item>
          </template>

          <v-list>
            <v-list-item
              :key="1"
              @click="cancelFriendship(item.id)"
            >
              <v-list-item-title>Remove from friend list</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>

        <span class="title ms-2"> Online users:</span>

        <v-menu
          v-for="(item, i) in onlineUsers"
          transition="slide-x-transition"
          :close-on-click="true"
          bottom
          right
        >
          <template v-slot:activator="{ on, attrs }">
            <v-list-item
              dark
              v-bind="attrs"
              v-on="on"
              exact
            >
              <v-list-item-action>
                <v-icon color="green" size="10">mdi-checkbox-blank-circle</v-icon>
              </v-list-item-action>
              <v-list-item-content>
                <v-list-item-title v-text="item.username"/>
              </v-list-item-content>
            </v-list-item>
          </template>

          <v-list>
            <v-list-item
              :key="1"
              @click="newFriendRequest(item)"
            >
              <v-list-item-title>Send friend request</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>

      </v-list>
    </v-navigation-drawer>
    <Footer/>
  </v-app>
</template>

<script>
import {mapState} from 'vuex'
import {mapGetters} from 'vuex'
import Snackbar from "@/components/Snackbar";

export default {
  components: {Snackbar},
  computed: {
    ...mapGetters({
      onlineUsers: 'users/otherOnlineUsers',
      onlineFriends: 'friends/onlineFriends',
      offlineFriends: 'friends/offlineFriends',
    }),
    ...mapState(["snackbar", "friends", "game"]),
  },
  async mounted() {
    let token = this.$auth.strategy.token.get()
    token = token.substring(7, token.length)
    await this.$store.dispatch("friends/fetchFriends")
    await this.$store.dispatch("websocket/sendToken")
  },
  methods: {
    async logout() {
      await this.$store.dispatch("users/reset")
      await this.$store.dispatch("websocket/resetToken")
      await this.$store.$auth.logout()
    },
    async newFriendRequest(user) {
      await this.$store.dispatch("friendrequests/newRequest", user.id, {root: true})
    },
    async removeFromFriendList(user) {
      await this.$store.dispatch("friend/newRequest", user.id, {root: true})
    },
    async cancelFriendship(userId) {
      await this.$store.dispatch("friends/cancel", userId)
    },
    async inviteToGame(userId) {
      await this.$store.dispatch("lobby/invite", userId)
    }
  },
  data() {
    return {
      closeOnClick: true,
      timeout: 5000,
      clipped: true,
      drawer: true,
      fixed: false,
      items: [
        {
          icon: 'mdi-gamepad',
          title: 'Game',
          to: '/newgame'
        },
        {
          icon: 'mdi-format-list-bulleted',
          title: 'Game history',
          to: '/history'

        },
        {
          icon: 'mdi-account-multiple',
          title: 'Friends',
          to: '/friends'
        },
        {
          icon: 'mdi-account',
          title: 'Profile',
          to: '/profile'
        }
      ],
      rightDrawer: true,
      right: true,
      title: 'Tic Tac Toe',
    }
  }
}
</script>

