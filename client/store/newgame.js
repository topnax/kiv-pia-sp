export const state = () => ({
  availableBoardSizes: [3, 5, 10],
  invites: [],
  invitesLoading: true,
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
  SET_INVITES(state, invites) {
    let lobbyIds = state.invites.map(invite => invite.lobbyId)
    invites.filter(invite => lobbyIds.indexOf(invite.lobbyId) === -1).forEach(invite => state.invites.push(invite))
  },

  REMOVE_INVITE(state, invite) {
    let inviteIndex = state.invites.map(invite => invite.lobbyId).indexOf(invite.lobbyId)

    if (inviteIndex !== -1) {
      state.invites.splice(inviteIndex, 1)
    }
  },

  SET_INVITES_LOADING(state, loading) {
    state.invitesLoading = loading
  }
}

export const actions = {
  async newInvite(context, invite) {
    await context.dispatch("snackbar/showInfo", `New invite to ${invite.lobbyId} by ${invite.ownerUsername}`, {root: true})
    context.commit("SET_INVITES", [invite])
  },
  async inviteGone(context, invite) {
    context.commit("REMOVE_INVITE", invite)
  },
  async create(context) {
    try {
      let result = await this.$axios.$post("/game/create", {
        boardSize: context.state.boardSize,
        victoriousCells: context.state.victoriousCells
      })

      if (result.responseCode === 0) {
        await this.$router.push("/game")
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      await context.dispatch("snackbar/showError", "Could not create a new game!", {root: true})
    }
  },
  async fetchInvites(context, data) {
    context.commit("SET_INVITES_LOADING", true)
    await new Promise(resolve => setTimeout(resolve, 200))
    try {
      let result = await this.$axios.$get("/game/invites")
      if (result.responseCode === 0) {
        await context.commit("SET_INVITES", result.data.invites)
        console.log(context.state.invites)
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      console.error(e)
      await context.dispatch("snackbar/showError", "Could not load game invites!", {root: true})
    }

    context.commit("SET_INVITES_LOADING", false)
  },
}

export const getters = {
  availableVictoriousCells: (state, getters, rootState, rootGetters) => {
    console.log("setting board size")
    console.log(state.boardSize)
    switch (state.boardSize) {
      case 3:
        return [3]
      case 5:
        return [3, 5]
      case 10:
        return [3, 5, 10]
    }
  }
}
