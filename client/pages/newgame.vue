<template>
  <v-container
    class="px-0"
    fluid
  >
    <v-row justify="center">
      <v-col md="4" class="">
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

      <v-col md="4" v-if="newgame.invites.length > 0">
        <v-card>
          <v-card-title>Game invitations</v-card-title>
          <v-card-text>
            <v-list>
              <v-list-item
                v-for="(invite) in newgame.invites"
              >
                <v-list-item-title>{{ invite.ownerUsername }} #{{ invite.lobbyId }}</v-list-item-title>
              </v-list-item>
            </v-list>
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
    this.$store.dispatch("newgame/fetchInvites")
  },
  methods: {
    create() {
      this.$store.dispatch("newgame/create")
    }
  },
  computed: {
    ...mapGetters({
      availableVictoriousCells: 'newgame/availableVictoriousCells',
    }),

    ...mapState(["newgame"]),
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
