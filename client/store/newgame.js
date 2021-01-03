export const state = () => ({
  availableBoardSizes: [3, 5, 10],
  boardSize: 1,
  victoriousCells: 3
})

export const mutations = {
  SET_VICTORIOUS_CELLS(state, victoriousCells) {
    if (victoriousCells <= state.boardSize) {
      console.log("setting victorious cells to")
      console.log(victoriousCells)
      state.victoriousCells = victoriousCells
    }
  },
  SET_BOARD_SIZE(state, boardSize) {
    if (state.availableBoardSizes.indexOf(boardSize) !== -1) {
      console.log("setting board size to")
      console.log(boardSize)
      state.boardSize = boardSize
    }
  },
}

export const actions = {
  async create(context) {
    console.log(context.state.boardSize)
    console.log(context.state.victoriousCells)
  },
}

export const getters = {
  availableVictoriousCells: (state, getters, rootState, rootGetters) => {
    console.log("setting board size")
    console.log(state.boardSize)
    switch (state.boardSize) {
      case 3: return [3]
      case 5: return [3, 5]
      case 10: return [3, 5, 10]
    }
  }
}
