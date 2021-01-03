package com.zcu.kiv.pia.tictactoe.model.response

import com.zcu.kiv.pia.tictactoe.game.TicTacToeGame
import com.zcu.kiv.pia.tictactoe.model.GameLobby
import com.zcu.kiv.pia.tictactoe.model.User

class PendingGameStateResponse(lobby: GameLobby, val owner: Boolean = false, invitedUsers: List<User> = listOf()) {
    val id = lobby.id
    val opponent = lobby.opponent
    val boardSize = lobby.boardSize
    val victoriousCells = lobby.boardSize
    val invitedUsers: List<Any> = invitedUsers.map {
        // TODO might provide ID to be able to cancel an invitation
        it.username
    }
}

class PlayingGameStateResponse(lobby: GameLobby, game: TicTacToeGame) {
    val turns = game.turns
    val boardSize = game.boardSize
    val ownerSeed = lobby.ownerSeed
    val opponentSeed = lobby.opponentSeed
    val opponentName = lobby.opponent!!.username
    val winner = game.winner
    val victoriousTurns = game.victoriousCells.toList()
}

class GameStateResponse(val stateType: StateType, val state: Any?) {
    enum class StateType {
        NONE,
        PENDING,
        PLAYING
    }
}

class GameInviteListResponse(invites: List<Pair<String, Int>>) {
    val invites: List<Any> = invites.map {
        object {
            val ownerUsername = it.first
            val lobbyId = it.second
        }
    }
}