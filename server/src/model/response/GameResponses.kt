package com.zcu.kiv.pia.tictactoe.model.response

import com.zcu.kiv.pia.tictactoe.game.TicTacToeGame
import com.zcu.kiv.pia.tictactoe.model.GameLobby

class PendingGameStateResponse (lobby: GameLobby) {
    val id = lobby.id
    val boardSize = lobby.boardSize
    val victoriousCells = lobby.boardSize
}

class PlayingGameStateResponse (game: TicTacToeGame) {
    val turns = game.turns
    val boardSize = game.boardSize
    val victoriousTurns = game.victoriousCells.toList()
    val winner = game.winner
}

class GameStateResponse(val stateType: StateType, val state: Any?) {
    enum class StateType{
        NONE,
        PENDING,
        PLAYING
    }
}