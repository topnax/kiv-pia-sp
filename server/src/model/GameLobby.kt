package com.zcu.kiv.pia.tictactoe.model

import com.zcu.kiv.pia.tictactoe.game.Seed
import com.zcu.kiv.pia.tictactoe.game.TicTacToeGame

class GameLobby(val id: Int, val owner: User, val boardSize: Int, val victoriousCells: Int) {
    var game: TicTacToeGame? = null
    var opponent: User? = null

    val ownerSeed = Seed.CROSS
    val opponentSeed = Seed.NOUGHT

    fun start(): Boolean {
        if (opponent == null || game != null) {
            return false
        }
        game = TicTacToeGame(boardSize, ownerSeed,victoriousCells)
        return true
    }
}