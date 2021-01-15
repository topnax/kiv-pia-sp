<template>
  <v-row no-gutters>
    <v-col md="4" sm="3" xs="12" cols="12" class="text-center">
      <v-chip
        class="ma-2"
        :color="crossWon ? 'green' : 'primary'"
        :outlined="(noughtsTurn || finished) && !crossWon"
      >
        <v-icon class="title me-3">{{!crossWon ? "mdi-close" : "mdi-trophy-variant"}}</v-icon> {{ crossSeedUsername }}
      </v-chip>
    </v-col>

    <v-col md="4" sm="6" xs="12" cols="12" class="text-center" justify="center">
      <v-chip
        v-if="usersTurn && !finished"
        class="ma-2"
        color="green"
        text-color="white"
      >
        You are playing...
      </v-chip>
      <v-chip
        v-if="draw && finished"
        class="ma-2"
        color="information"
        text-color="white"
      >
        <v-icon class="me-2">mdi-handshake</v-icon> It's a draw
      </v-chip>
      <v-chip
        v-if="noughtWon && finished"
        class="ma-2"
        color="green"
        outlined
      >
        <v-icon class="me-2">mdi-party-popper</v-icon><strong>{{noughtSeedUsername}}</strong>&nbsphas won the game!
      </v-chip>

      <v-chip
        v-if="crossWon && finished"
        class="ma-2"
        color="green"
        outlined
      >
        <v-icon class="me-2">mdi-party-popper</v-icon><strong>{{crossSeedUsername}}</strong>&nbsphas won the game!
      </v-chip>


    </v-col>
    <v-col md="4" sm="3" xs="12" cols="12" class="text-center">
      <v-chip
        class="ma-2"
        :color="noughtWon ? 'green' : 'primary'"
        :outlined="(crossTurn || finished) && !noughtWon"
      >
        <v-icon class="title me-3">{{!noughtWon ? "mdi-circle-outline" : "mdi-trophy-variant"}}</v-icon> {{ noughtSeedUsername }}
      </v-chip>
    </v-col>
  </v-row>
</template>

<script>
export default {
  name: 'GameHeader',
  props: {
    draw: false,
    noughtsTurn: false,
    crossTurn: false,
    usersTurn: false,
    noughtSeedUsername: "",
    crossSeedUsername: "",
    finished: false,
    winner: ""
  },
  methods: {
    async leave() {
      await this.$store.dispatch("lobby/leave")
    },
    async start() {
      await this.$store.dispatch("lobby/start")
    }
  },
  computed: {
    noughtWon() {
      return !this.$props.finished ? false : (this.$props.winner === 'O')
    },
    crossWon() {
      return !this.$props.finished ? false : (this.$props.winner === 'X')
    },
  }

}
</script>

