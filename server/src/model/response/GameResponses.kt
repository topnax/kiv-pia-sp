package com.zcu.kiv.pia.tictactoe.model.response

import com.zcu.kiv.pia.tictactoe.game.TicTacToeGame
import com.zcu.kiv.pia.tictactoe.model.GameLobby

class PendingGameStateResponse(lobby: GameLobby) {
    val id = lobby.id
    val boardSize = lobby.boardSize
    val victoriousCells = lobby.boardSize
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