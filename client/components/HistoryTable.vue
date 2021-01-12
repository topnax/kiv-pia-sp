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
    <template v-slot:item.result="{ item }">
      <v-icon v-if="item.result === 1" color="yellow">
        mdi-trophy-variant
      </v-icon>
      <v-icon v-else-if="item.result === 2" color="error">
        mdi-cancel
      </v-icon>
      <v-icon v-else
      color="gray">
        mdi-handshake
      </v-icon>
    </template>
    <template v-slot:item.winner="{ item }">
      <v-icon v-if="item.winner === 'X'">
        mdi-close
      </v-icon>
      <v-icon v-else-if="item.winner === 'O'">
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
      {text: 'Result', value: 'result'},
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
