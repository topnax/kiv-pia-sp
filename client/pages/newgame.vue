<template>
  <v-container
    class="px-0"
    fluid
  >
    <v-row justify="center">
      <pending v-if="lobby.lobbySet" :boardSize="lobby.lobby.boardSize"
               :victoriousCells="lobby.lobby.victoriousCells" :id="lobby.lobby.id"
               :invitedUsers="lobby.lobby.invitedUsers"
               :owner="lobby.lobby.owner"
               :ownerUsername="lobby.lobby.ownerUsername"
               :opponentUsername="lobby.lobby.opponentUsername"/>
      <div v-else-if="game.in_game">
        <v-row v-if="game.finished">
          <v-col align="center">
            <h3 v-if="userWon" class="success--text">You have won!</h3>
            <h3 v-else-if="!game.draw" class="error--text">You have lost :(</h3>
            <v-btn class="mt-2" @click="finishedGameClose">Close</v-btn>
          </v-col>
        </v-row>
        <v-row v-else>
          <v-col align="center">
              <v-btn text @click="surrender">SURRENDER</v-btn>
          </v-col>
        </v-row>
        <game-header  :noughtsTurn="noughtsTurn" :crossTurn="crossTurn" :finished="game.finished"
                     :noughtSeedUsername="noughtSeedUsername" :crossSeedUsername="crossSeedUsername" :winner="game.playing.winner" :usersTurn="usersTurn && !game.finished" :draw="game.draw"/>
        <board :squares="cells" :size="game.playing.boardSize" :victoriousCells="victoriousCellsGame" :onCellClick="click"/>
      </div>

      <v-col md="4" class="" v-else>
        <v-card>
          <v-card-title>Create a new game</v-card-title>
          <v-card-text>
            <span class="subtitle-1">Board size:</span>
            <v-radio-group v-model="boardSize">
              <v-radio
                v-for="n in newgame.availableBoardSizes"
                :key="n"
                :value="n"
                :label="`${n} x ${n}`"
              ></v-radio>
            </v-radio-group>
            <div v-if="availableVictoriousCells !== undefined && availableVictoriousCells.length > 0">
              <span class="subtitle-1">Winning squares:</span>
              <v-radio-group v-model="victoriousCells">
                <v-radio
                  v-for="n in availableVictoriousCells"
                  :key="n"
                  :label="`${n}`"
                  :value="n"
                ></v-radio>
              </v-radio-group>
            </div>
          </v-card-text>
          <v-card-actions>
            <v-btn text
                   @click="create">CREATE
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>

      <v-col md="4" v-if="lobby.invites.length > 0 && !game.in_game">
        <v-card>
          <v-card-title>Game invitations</v-card-title>
          <v-card-text>
            <v-simple-table>
              <template v-slot:default>
                <thead>
                <tr>
                  <th class="text-left">
                    Lobby owner
                  </th>
                  <th class="text-right">
                  </th>
                </tr>
                </thead>
                <tbody>
                <tr
                  v-for="invite in lobby.invites"
                  :key="invite.lobbyId"
                >
                  <td>{{ invite.ownerUsername }}</td>
                  <td class="text-right">
                    <v-icon @click="acceptInvite(invite.lobbyId)">mdi-check</v-icon>
                    <v-icon @click="declineInvite(invite.lobbyId)">mdi-cancel</v-icon>
                  </td>
                </tr>
                </tbody>
              </template>
            </v-simple-table>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import {mapGetters, mapState} from "vuex";

export default {
  async mounted() {
    await this.$store.dispatch("lobby/fetchInvites")
    if (this.$store.state.websocket.socket.isConnected === false) {
      await new Promise(resolve => {
        const watcher = this.$root.$watch(() => this.$store.state.websocket.socket.isConnected, (newVal) => {
          if (newVal === true) {
            resolve(newVal);
            watcher(); // cleanup;
          }

        });
      })
    }
    await this.$store.dispatch("game/refresh")
  },
  methods: {
    surrender(){
      this.$store.dispatch("game/surrender")
    },
    finishedGameClose() {
      this.$store.dispatch("game/finishedGameClose")
    },
    create() {
      this.$store.dispatch("newgame/create")
    },
    acceptInvite(lobbyId) {
      this.$store.dispatch("lobby/acceptInvite", lobbyId)
    },
    declineInvite(lobbyId) {
      this.$store.dispatch("lobby/declineInvite", lobbyId)
    },
    click(index, row) {
      //this.$store.dispatch("game/move", {index: this.indexByRow(index, row, this.size), seed: "O"})
      this.$store.dispatch("game/play", {row: row, column: index})
//      this.$emit('click', this.indexByRow(index, row));
    }
  },
  computed: {
    ...mapGetters({
      availableVictoriousCells: 'newgame/availableVictoriousCells',
      cells: 'game/cells',
      victoriousCellsGame: 'game/victoriousCells',
      noughtSeedUsername: 'game/noughtSeedUsername',
      crossSeedUsername: 'game/crossSeedUsername',
      usersTurn: 'game/usersTurn',
      noughtsTurn: 'game/noughtsTurn',
      crossTurn: 'game/crossTurn',
      userWon: 'game/userWon',
      crossWon: 'game/crossWon',
      noughtWon: 'game/noughtWon'
    }),

    ...mapState(["newgame", "lobby", "game"]),
    victoriousCells: {
      get() {
        return this.$store.state.victoriousCells;
      },
      set(value) {
        this.$store.commit("newgame/SET_VICTORIOUS_CELLS", value);
      }
    },
    boardSize: {
      get() {
        return this.$store.state.boardSize;
      },
      set(value) {
        this.$store.commit("newgame/SET_BOARD_SIZE", value);
      }
    }
  },
  layout: "user_loggedin",
  name: "newgame",
  middleware: ["auth"]
}
</script>

<style scoped>

</style>
