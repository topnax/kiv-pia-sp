package com.zcu.kiv.pia.tictactoe.model.response

import com.zcu.kiv.pia.tictactoe.game.Seed
import com.zcu.kiv.pia.tictactoe.game.TicTacToeGame
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.service.game.GameWrapper

class GameStateResponse(gameWrapper: GameWrapper, opponent: User) {
    val turns: List<Any> = gameWrapper.game.turns.map {
       object {
           val row = it.row
           val column = it.column
           val seed = it.seed.toString()
       }
    }.toList()

    val opponentSeed = if (gameWrapper.cross == opponent) "X" else "O"
    val playerSeed = if (opponentSeed == "X") "0" else "X"
    val opponentUsername = opponent.username
    val boardSize = gameWrapper.game.boardSize
    val victoriousCells = gameWrapper.game.victoriousCells

}

class NewTurnResponse(val row: Int, val column: Int, seed: Seed) {
   val seed = seed.toString()
}

class GameDrawResponse()

class GameWonResponse(game: TicTacToeGame) {
    val winnerSeed = game.winner.toString()
    val victoriousCells: List<Any> = game.victoriousCells.map {
        object {
            val row = it.row
            val column = it.column
        }
    }.toList()
}
