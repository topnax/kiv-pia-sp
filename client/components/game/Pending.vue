<template>
  <v-row justify="center">
    <v-col md="4">
      <v-card>
        <v-card-title>
          Game lobby #{{ id }}
        </v-card-title>

        <v-card-subtitle >
          <span v-if="opponentUsername === undefined">
            Please invite a user to play the game!
          </span>
          <span v-else-if="owner">
            About to play against <strong>{{opponentUsername}}</strong>
          </span>
          <span v-else>
            About to play against <strong>{{ownerUsername}}</strong>
          </span>
        </v-card-subtitle>

        <v-card-text>
          <strong>Board size:</strong> {{ boardSize }}<br>
          <strong>Victorious cells:</strong> {{ victoriousCells }}<br>
          <div v-if="owner && invitedUsers !== undefined && invitedUsers.length > 0">
            <strong>Invited users:</strong><br>
            <ul v-for="user in invitedUsers">
             <li><span >{{user}}<br></span></li>
            </ul>
          </div>
        </v-card-text>
        <v-card-actions>
          <v-btn text v-if="owner" @click="start">START</v-btn>
          <v-btn text
                 @click="leave">LEAVE
          </v-btn>
        </v-card-actions>

      </v-card>
    </v-col>
  </v-row>
</template>

<script>
export default {
  name: 'Pending',
  props: {
    id: 0,
    boardSize: Number,
    victoriousCells: Number,
    invitedUsers: Array,
    owner: Boolean,
    opponentUsername: String,
    ownerUsername: String
  },
  methods: {
    async leave() {
      await this.$store.dispatch("lobby/leave")
    },
    async start() {
      await this.$store.dispatch("lobby/start")
    }
  }

}
</script>

