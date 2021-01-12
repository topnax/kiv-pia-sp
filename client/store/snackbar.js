export const state = () => ({
  snackbars: [],
  nextSnackbarId: 0
})

export const mutations = {
  SET_SNACKBAR(state, payload) {
    console.log(payload)
    state.snackbars = state.snackbars.concat({
      id: state.nextSnackbarId,
      showing: payload.showing,
      color: payload.color,
      text: payload.text
    })
    state.nextSnackbarId++
  },
  REMOVE_SNACKBAR(state, id) {
    let index = state.snackbars.map(snackbar => snackbar.id).indexOf(id)
    if (index !== -1) {
      console.log("removing")
      state.snackbars.splice(index, 1)
    }
  }
}

export const actions = {

  removeSnackbar: (context, id) => {
   context.commit("REMOVE_SNACKBAR", id)
  },
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
