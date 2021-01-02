import Vue from 'vue'

export const state = () => ({
  size: Number,
  seed: String,
  squares: Array(25).fill(null),
})

export const mutations = {
    SET_SQUARE(state, {index, seed}) {
      state.squares[index] = seed
      Vue.set(state.squares, index, seed)
      console.log(`at index=${index}`)
      console.log(state.squares[index])
    }
}

export const actions = {
  async move(context, data) {
      await context.commit("SET_SQUARE", data)
  },
}
