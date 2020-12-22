package com.zcu.kiv.pia.tictactoe.model

import com.zcu.kiv.pia.tictactoe.game.TicTacToeGame

class Game(val id: Int, val owner: User, val boardSize: Int) {
    var game: TicTacToeGame? = null
    var opponent: User? = null
}