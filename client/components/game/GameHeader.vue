<template>
  <v-row>
    <v-col md="4">
      <v-chip
        class="ma-2"
        :color="crossWon ? 'green' : 'primary'"
        :outlined="(noughtsTurn || finished) && !crossWon"
      >
        <v-icon class="title me-3">{{!crossWon ? "mdi-close" : "mdi-trophy-variant"}}</v-icon> {{ crossSeedUsername }}
      </v-chip>
    </v-col>

    <v-col md="4" class="text-center" justify="center">
      <v-chip
        v-if="usersTurn && !finished"
        class="ma-2"
        color="green"
        text-color="white"
      >
        You are playing...
      </v-chip>
    </v-col>
    <v-col md="4" class="text-center">
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

