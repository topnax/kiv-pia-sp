package com.zcu.kiv.pia.tictactoe.model.response.realtime

import com.zcu.kiv.pia.tictactoe.model.Lobby

class NewInviteResponse(val lobbyId: Int, val ownerUsername: String)

class LobbyStateResponse(lobby: Lobby, val owner: Boolean) {
    val id = lobby.id
    val opponent = lobby.opponent
    val boardSize = lobby.boardSize
    val victoriousCells = lobby.victoriousCells
    val invitedUsers = lobby.invitedUsers.map {
        it.username
    }.toList()
}
