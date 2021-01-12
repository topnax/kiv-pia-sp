<template>
  <v-container justify="center">
  <user-table :users="useradministration.users" :loading="useradministration.loading"/>
  </v-container>
</template>

<script>
import UserTable from "@/components/UserTable";
import {mapState} from "vuex";

export default {
  mounted() {
    this.$store.dispatch("useradministration/fetchUsers")
  },
  computed: {
    ...mapState(["useradministration"]),
  },
  components: {UserTable},
  layout: "user_loggedin",
  name: "user-administration",
  middleware: ["auth", "auth-admin"]
}
</script>

<style scoped>

</style>
