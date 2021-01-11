<template>
  <v-data-table
    :headers="headers"
    :items="games"
    :items-per-page="5"
    :loading="loading"
    loading-text="Loading... Please wait"
    class="elevation-1"
    @click:row="onRowClick"
  >
    <template v-slot:item.won="{ item }">
      <v-icon v-if="item.won" color="yellow">
        mdi-trophy-variant
      </v-icon>
      <v-icon v-else color="error">
        mdi-cancel
      </v-icon>
    </template>
    <template v-slot:item.winner="{ item }">
      <v-icon v-if="item.winner === 'X'">
        mdi-close
      </v-icon>
      <v-icon v-else>
        mdi-circle-outline
      </v-icon>
    </template>
  </v-data-table>
</template>

<script>
export default {
  name: "HistoryList",

  props: {
    games: null,
    loading: true,
    onRowClick: null
  },
  data: () => ({
    headers: [
      {text: 'Won', value: 'won'},
      {text: 'Opponent username', value: 'opponentUsername'},
      {text: 'Winner', value: 'winner'},
      {text: 'Board size', value: 'boardSize'},
      {
        text: 'Date',
        align: 'start',
        value: 'dateCreated',
      },
    ],
  })
}
</script>
