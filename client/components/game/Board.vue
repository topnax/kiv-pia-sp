<template>

  <v-container fill-height fluid>
    <v-row align="center"
           justify="center">


      <div :class="'board board-' + size" v-if="squares">
        <div v-for="row in size" :key="row" :class="`board-` + size + `-row board-row`">
          <square v-for="i in size" :key="indexByRow(i, row)"
                  :value="squares[indexByRow(i, row)]"
                  :disabled="!!winner"
                  :winner="!!winner && winner.includes(indexByRow(i, row))"
                  @click="click(i, row)"/>
        </div>
      </div>
    </v-row>
  </v-container>

</template>

<script>
export default {
  name: 'Board',
  props: {
    size: Number,
    squares: Array,
    winner: Array
  },
  components: {
    Square: () => import('./Cell')
  },
  methods: {
    indexByRow(index, row, max = 3) {
      return (row * max + index) - (max + 1)
    },
    click(index, row) {
//      this.$emit('click', this.indexByRow(index, row));
    }
  }
}
</script>

<style scoped>

.board-3 {
  grid-template-rows: repeat(3, 1fr);
}
.board-3-row {
  grid-template-columns: repeat(3, 1fr);
}

.board-5 {
  grid-template-rows: repeat(5, 1fr);
}
.board-5-row {
  grid-template-columns: repeat(5, 1fr);
}

.board-10 {
  grid-template-rows: repeat(10, 1fr);
}
.board-10-row {
  grid-template-columns: repeat(10, 1fr);
}

.board {
  border: 1rem solid #fff4;
  box-shadow: 2.5px 5px 25px #0004, 0 1px 6px #0006;
  border-radius: .5rem;
  width: 65vmin;
  height: 65vmin;
  display: grid;
  grid-template-columns: 1fr;
  backdrop-filter: blur(10px);
  background-blend-mode: exclusion;
}

.board-row {
  display: grid;
  grid-template-rows: 1fr;
}
</style>
