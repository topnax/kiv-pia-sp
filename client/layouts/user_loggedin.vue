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
      <span class="ms-3">{{$store.$auth.user.username}} ({{$store.$auth.user.email}})</span>
      <v-btn
        icon
        @click.stop="rightDrawer = !rightDrawer"
      >
        <v-icon v-if="rightDrawer">mdi-chevron-right</v-icon>
        <v-icon v-else>mdi-account-multiple</v-icon>
      </v-btn>
    </v-app-bar>
    <v-main>
      <v-container>
        <nuxt/>
        <v-snackbar
          v-model="showing"
          :timeout="timeout"
          :color="snackbar.color"
        >
          {{ snackbar.text }}
          <v-btn
            color="white"
            text
            @click="showing = false"
            align-end
          >
            Close
          </v-btn>
        </v-snackbar>
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

        <span class="title ms-2 mt-10 pt-10">Friends:</span>
        <v-list-item
          v-for="(item, i) in onlineFriends"
          exact
        >
          <v-list-item-action>
            <v-icon color="green" size="10">mdi-checkbox-blank-circle</v-icon>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title v-text="item.name"/>
          </v-list-item-content>
        </v-list-item>
        <v-list-item
          v-for="(item, i) in offlineFriends"
          onclick="alert('hello')"
          exact
        >
          <v-list-item-action>
            <v-icon color="red" size="10">mdi-checkbox-blank-circle</v-icon>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title v-text="item.name"/>
          </v-list-item-content>
        </v-list-item>
        <span class="title ms-2"> Online users:</span>
        <v-list-item
          v-for="(item, i) in onlineUsers"
          onclick="alert('hello')"
          exact
        >
          <v-list-item-action>
            <v-icon color="green" size="10">mdi-checkbox-blank-circle</v-icon>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title v-text="item.name"/>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>
    <Footer/>
  </v-app>
</template>

<script>
import {mapState} from 'vuex'

export default {
  computed: {
    showing: {
      get() {
        return this.$store.state.snackbar.showing
      },
      set(v) {
        this.$store.commit('snackbar/SET_SNACKBAR', {showing: v})
      }
    },
    ...mapState(["snackbar"])

  },
  methods: {
    async logout() {
      await this.$store.$auth.logout()
    }
  },
  data() {
    return {
      timeout: 2000,
      clipped: true,
      drawer: true,
      fixed: false,
      items: [
        {
          icon: 'mdi-account',
          title: 'Profile',
          to: '/profile'
        },
        {
          icon: 'mdi-account-multiple',
          title: 'Friends',
          to: '/friends'
        }
      ],
      rightDrawer: true,
      right: true,
      title: 'Tic Tac Toe',
      onlineFriends: [
        {
          name: "vojta33"
        },
        {
          name: "Pipous3k"
        }
      ],
      offlineFriends: [
        {
          name: "Papi"
        },
        {
          name: "Gebron"
        }
      ],
      onlineUsers: [
        {
          name: "topnax"
        },
        {
          name: "JIGGERS"
        },
        {
          name: "Paagrio"
        },
      ]
    }
  }
}
</script>
