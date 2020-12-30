<template>
  <v-row class="pt-10" justify="center">
    <v-col md="4">
     <v-card
       v-if="friendsLoading"
     >
       <v-card-text><v-progress-linear
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
                <v-icon @click="cancelFriendship(friend.id)">mdi-cancel</v-icon>
              </td>
            </tr>
            </tbody>
          </template>
        </v-simple-table>
      </div>
    </v-col>
    <v-col class="text-center " md="4" v-if="requests.length !== 0">
      <v-card
        v-for="(request, i) in requests"
        color="#385F73"
        dark
      >
        <v-card-text class="text-left"><v-icon class="me-2">mdi-heart</v-icon><strong>{{ request.requestorUsername }}</strong> wants to become friends!</v-card-text>
        <v-card-actions>
          <v-btn text @click="acceptRequest(request.requestId)">Accept</v-btn>
          <v-btn text @click="declineRequest(request.requestId)">Decline</v-btn>
        </v-card-actions>
      </v-card>
    </v-col>
  </v-row>
</template>

<script>
import {mapState} from 'vuex'

export default {
  layout: "user_loggedin",
  name: "profile",
  middleware: ["auth"],
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
    cancelFriendship(userId) {
      this.$store.dispatch("friends/cancel", userId)
    }
  }
}
</script>

<style scoped>

</style>
