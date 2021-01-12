export const state = () => ({
  snackbars: [],
})

export const mutations = {
  SET_SNACKBAR(state, payload) {
    console.log(payload)
    state.snackbars = state.snackbars.concat({
      showing: payload.showing,
      color: payload.color,
      text: payload.text
    })
    console.log(state.snackbars)
  }
}

export const actions = {
  showInfo: (context, text) => {
    context.commit("SET_SNACKBAR", {
      showing: true,
      text: text,
      color: "info"
    })
  },
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
