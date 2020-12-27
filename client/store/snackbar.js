export const state = () => ({
  showing: false,
  color: "success",
  text: ""
})

export const mutations = {
  SET_SNACKBAR(state, payload) {
    state.showing = payload.showing
    state.color = payload.color
    state.text = payload.text
  }
}

export const actions = {
  showSuccess: (context, text) => {
    context.commit("SET_SNACKBAR", {
      showing: true,
      text: text,
      color: "success"
    })
  },
  showError: (context, text) => {
    context.commit("SET_SNACKBAR", {
      showing: true,
      text: text,
      color: "error"
    })
  }
}
