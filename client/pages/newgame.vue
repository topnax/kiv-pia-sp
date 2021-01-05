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

      <v-col md="4" v-if="lobby.invites.length > 0">
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
  mounted() {
    this.$store.dispatch("lobby/fetchInvites")
    this.$store.dispatch("game/refresh")
  },
  methods: {
    create() {
      this.$store.dispatch("newgame/create")
    },
    acceptInvite(lobbyId) {
      this.$store.dispatch("lobby/acceptInvite", lobbyId)
    },
    declineInvite(lobbyId) {
      this.$store.dispatch("lobby/declineInvite", lobbyId)
    }
  },
  computed: {
    ...mapGetters({
      availableVictoriousCells: 'newgame/availableVictoriousCells',
    }),

    ...mapState(["newgame", "lobby"]),
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
