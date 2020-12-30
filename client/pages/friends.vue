<template>
  <v-row class="pt-10">
    <v-col class="text-center " md="4" v-if="!loading">
      <span v-if="requests.length === 0">No friend requests found :(</span>
      <v-card
        v-else
        v-for="(request, i) in requests"
        color="#385F73"
        dark
        class="mt-5"
      >
        <v-card-title class="headline">{{ request.requestorUsername}} #{{request.requestId}}</v-card-title>


        <v-card-actions>
          <v-btn text @click="acceptRequest(request.requestId)">Accept</v-btn>
          <v-btn text @click="declineRequest(request.requestId)">Decline</v-btn>
        </v-card-actions>
      </v-card>
    </v-col>
    <v-col class="mt-10 mx-auto" md="5" v-else>
      <v-progress-linear
        indeterminate
        color="teal"
      ></v-progress-linear>
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
      loading: state => state.friendrequests.loading
    })
  },
  mounted() {
    console.log("about to dispatch")
    this.$store.dispatch("friendrequests/fetchRequests")
  },
  methods : {
    acceptRequest(requestId) {
      this.$store.dispatch("friendrequests/acceptRequest", requestId)
    },
    declineRequest(requestId) {
      this.$store.dispatch("friendrequests/declineRequest", requestId)
    }
  }
}
</script>

<style scoped>

</style>
