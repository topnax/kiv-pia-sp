<template>
  <v-row>
    <v-col>
      <history-table :games="gamesParsed" :loading="history.loading"/>
    </v-col>
  </v-row>
</template>

<script>
import {mapGetters, mapState} from "vuex";

export default {
  components: {
    HistoryTable: () => import('../components/HistoryTable'),
  },
  mounted() {
    this.$store.dispatch("history/fetchHistory")
  },
  computed: {
    ...mapState(["history"]),
    ...mapGetters({
      gamesParsed: 'history/gamesParsed',
    }),
  },
  layout: "user_loggedin",
  name: "history",
  middleware: ["auth"]
}
</script>
