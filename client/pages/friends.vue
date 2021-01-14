<template>
  <v-container>
    <friend-removal-dialog :display="displayRemovalDialog" :friend-username="friendToBeRemovedUsername"
                           :on-cancel-clicked="removalCancelled" :on-confirmation-clicked="removalConfirmed"/>
    <v-row class="pt-10" justify="center">
      <v-col md="4" sm="12" cols="12">
        <v-card
          v-if="friendsLoading"
        >
          <v-card-text>
            <v-progress-linear
              indeterminate
              color="teal"
            ></v-progress-linear>
          </v-card-text>
        </v-card>


        <div v-else>
          <v-card v-if="friends.length === 0">
            <v-card-text class="text-center">No friends found :(</v-card-text>
          </v-card>
          <v-simple-table v-else>
            <template v-slot:default>
              <thead>
              <tr>
                <th class="text-left">
                  Username
                </th>
                <th class="text-right">
                </th>
              </tr>
              </thead>
              <tbody>
              <tr
                v-for="friend in friends"
                :key="friend.id"
              >
                <td>{{ friend.username }}</td>
                <td class="text-right">
                  <v-icon @click="cancelFriendship(friend.id, friend.username)">mdi-cancel</v-icon>
                </td>
              </tr>
              </tbody>
            </template>
          </v-simple-table>
        </div>
      </v-col>
      <v-col class="text-center" md="4" sm="12" cols="12" v-if="requests.length !== 0">
        <v-card
          v-for="(request, i) in requests"
          color="#385F73"
          dark
        >
          <v-card-text class="text-left">
            <v-icon class="me-2">mdi-heart</v-icon>
            <strong>{{ request.requestorUsername }}</strong> wants to become friends!
          </v-card-text>
          <v-card-actions>
            <v-btn text @click="acceptRequest(request.requestId)">Accept</v-btn>
            <v-btn text @click="declineRequest(request.requestId)">Decline</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import {mapState} from 'vuex'
import FriendRemovalDialog from "@/components/FriendRemovalDialog";

export default {
  components: {FriendRemovalDialog},
  layout: "user_loggedin",
  name: "profile",
  middleware: ["auth"],
  data: () => ({
    friendToBeRemovedUsername: "",
    displayRemovalDialog: false,
    userIdToBeRemoved: -1,
  }),
  computed: {
    ...mapState({
      requests: state => state.friendrequests.requests,
      loading: state => state.friendrequests.loading,
      friendsLoading: state => state.friends.loading,
      friends: state => state.friends.friends
    })
  },
  mounted() {
    this.$store.dispatch("friendrequests/fetchRequests")
    this.$store.dispatch("friends/fetchFriends")
  },
  methods: {
    acceptRequest(requestId) {
      this.$store.dispatch("friendrequests/acceptRequest", requestId)
    },
    declineRequest(requestId) {
      this.$store.dispatch("friendrequests/declineRequest", requestId)
    },
    cancelFriendship(userId, username) {
      this.userIdToBeRemoved = userId
      this.friendToBeRemovedUsername = username
      this.displayRemovalDialog = true
    },
    removalConfirmed() {
      this.$store.dispatch("friends/cancel", this.userIdToBeRemoved)
      this.displayRemovalDialog = false
    },
    removalCancelled() {
      this.displayRemovalDialog = false
    }
  }
}
</script>

<style scoped>

</style>
