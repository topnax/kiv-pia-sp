export const state = () => ({
  invites: [],
  invitesLoading: true,
  lobbySet: false,
  lobby: {
    id: Number,
    opponentUsername: String,
    boardSize: Number,
    victoriousCells: Number,
    invitedUsers: Array,
  }
})

export const mutations = {
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
  },

  SET_LOBBY_STATE(state, lobbyState) {
    state.lobbySet = true
    state.lobby = {}
    state.lobby.id = lobbyState.id
    state.lobby.opponentUsername = lobbyState.opponentUsername
    state.lobby.ownerUsername = lobbyState.ownerUsername
    state.lobby.boardSize = lobbyState.boardSize
    state.lobby.victoriousCells = lobbyState.victoriousCells
    state.lobby.invitedUsers = lobbyState.invitedUsers
    state.lobby.owner = lobbyState.owner
    console.log("lobby state set to ")
    console.log(state.lobby)
  },

  DESTROY_LOBBY(state) {
    state.lobbySet = false
    state.lobby = undefined
  }
}

export const actions = {

  async leave(context, data) {
    try {
      let result = await this.$axios.$post("/lobby/leave", {lobbyId: context.state.lobby.id})

      if (result.responseCode === 0) {
        context.commit("DESTROY_LOBBY")
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      await context.dispatch("snackbar/showError", "Could not leave game", {root: true})
    }
  },

  async newInvite(context, invite) {
    await context.dispatch("snackbar/showInfo", `New invite to ${invite.lobbyId} by ${invite.ownerUsername}`, {root: true})
    context.commit("SET_INVITES", [invite])
  },
  async goneInvite(context, invite) {
    context.commit("REMOVE_INVITE", invite)
  },

  async start(context, lobbyId) {
    try {
      let result = await this.$axios.$post("/lobby/start", {
        lobbyId: context.state.lobby.id
      })

      if (result.responseCode !== 0) {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      await context.dispatch("snackbar/showError", "Could not start the lobby", {root: true})
    }
  },
  async acceptInvite(context, lobbyId) {
    try {
      let result = await this.$axios.$post("/lobby/accept", {
        lobbyId: lobbyId
      })

      if (result.responseCode !== 0) {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      await context.dispatch("snackbar/showError", "Could not accept the invite.", {root: true})
    }
  },
  async declineInvite(context, lobbyId) {
    try {
      let result = await this.$axios.$post("/lobby/decline", {
        lobbyId: lobbyId
      })

      if (result.responseCode !== 0) {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      await context.dispatch("snackbar/showError", "Could not cancel the invite.", {root: true})
    }
  },
  async fetchInvites(context, data) {
    context.commit("SET_INVITES_LOADING", true)
    await new Promise(resolve => setTimeout(resolve, 200))
    try {
      let result = await this.$axios.$get("/lobby/invites")
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
  async destroyed(context, data) {
    context.commit("DESTROY_LOBBY")
  },
  async state(context, data) {
    console.log("state:")
    console.log(data)
    context.commit("SET_LOBBY_STATE", data)
  },
  async invite(context, userId) {
    try {
      let result = await this.$axios.$post("/lobby/invite", {userId: userId})

      if (result.responseCode === 0) {
        await context.dispatch("snackbar/showSuccess", "Friend invited", {root: true})
      } else {
        await context.dispatch("snackbar/showError", result.message, {root: true})
      }
    } catch (e) {
      await context.dispatch("snackbar/showError", "Could not invite a friend", {root: true})
    }
  },

}

export const getters = {
}
