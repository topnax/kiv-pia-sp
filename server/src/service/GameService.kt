package com.zcu.kiv.pia.tictactoe.service

import com.zcu.kiv.pia.tictactoe.database.logger
import com.zcu.kiv.pia.tictactoe.game.TicTacToeGame
import com.zcu.kiv.pia.tictactoe.model.GameLobby
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.model.response.*
import java.lang.Exception

interface GameService {
    fun isItUsersTurn(user: User, gameLobby: GameLobby): Boolean

    fun placeSeed(user: User, row: Int, column: Int): Boolean

}

class GameRepository {
    fun getGame() = "Tic Tac Toe game played :)"
}

class GameServiceImpl(private val gameRepository: GameRepository, private val realtimeService: RealtimeService) :
    GameService {

    private var availableId = 0

    /**
     * Users are being mapped to list of lobby owners
     */
    private var gameInvites = mutableMapOf<User, MutableList<GameLobby>>()

    /**
     * User IDs to a game they participate in
     */
    private val userToGames = hashMapOf<Int, GameLobby>()

    /**
     * Game lobbied mapped by IDs
     */
    private val games = hashMapOf<Int, GameLobby>()

    override fun isItUsersTurn(user: User, gameLobby: GameLobby): Boolean {
        val userSeed = if (user == gameLobby.owner) gameLobby.ownerSeed else gameLobby.opponentSeed
        return gameLobby.game?.currentSeed == userSeed
    }

    override fun placeSeed(user: User, row: Int, column: Int): Boolean {
        val game = userToGames[user.id]

        logger.debug { "game is null? ${game == null}" }

        if (game?.game == null) return false

        logger.debug { "game.game is null - no!" }
        val seed = if (game.owner == user) game.ownerSeed else game.opponentSeed
        val added = game.game?.addSeed(row, column, seed) ?: false
        // TODO check whether the game has ended
        return added
    }

}