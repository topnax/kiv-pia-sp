package com.zcu.kiv.pia.tictactoe.model.response.realtime

import com.zcu.kiv.pia.tictactoe.model.Lobby

class NewInviteResponse(val lobbyId: Int, val ownerUsername: String)

class LobbyStateResponse(lobby: Lobby, val owner: Boolean) {
    val id = lobby.id
    val opponentUsername = lobby.opponent?.username
    val ownerUsername = lobby.owner.username
    val boardSize = lobby.boardSize
    val victoriousCells = lobby.victoriousCells
    val invitedUsers = lobby.invitedUsers.map {
        it.username
    }.toList()
}

class InviteGoneResponse(val lobbyId: Int)

class LobbyDestroyedResponse(val lobbyId: Int)

class LobbyInvitesResponse(invites: List<Pair<String, Int>>) {
    val invites: List<Any> = invites.map {
        object {
            val ownerUsername = it.first
            val lobbyId = it.second
        }
    }
}
