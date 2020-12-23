package com.zcu.kiv.pia.tictactoe.service

import com.zcu.kiv.pia.tictactoe.database.logger
import com.zcu.kiv.pia.tictactoe.model.GameLobby
import com.zcu.kiv.pia.tictactoe.model.User

interface GameService {
    fun playGame(): String

    fun createGame(user: User, boardSize: Int, victoriousCells: Int): Boolean

    fun addUserToAGame(user: User, gameId: Int): Boolean

    fun isItUsersTurn(user: User, gameLobby: GameLobby): Boolean

    fun removeUserFromAGame(user: User, gameId: Int): Boolean

    fun removeUserFromAGame(user: User): Boolean

    fun isUserInAGame(user: User): Boolean

    fun placeSeed(user: User, row: Int, column: Int): Boolean

    fun startGame(gameLobby: GameLobby): Boolean

    fun getGameByUser(user: User): GameLobby?
}

class GameRepository {
    fun getGame() = "Tic Tac Toe game played :)"
}

class GameServiceImpl(private val gameRepository: GameRepository) : GameService {

    private var availableId = 0

    /**
     * User IDs to a game they participate in
     */
    private val userToGames = hashMapOf<Int, GameLobby>()

    private val games = hashMapOf<Int, GameLobby>()

    override fun createGame(user: User, boardSize: Int, victoriousCells: Int): Boolean {
        if (isUserInAGame(user)) return false

        val game = GameLobby(availableId, user, boardSize, victoriousCells)
        games[availableId] = game
        userToGames[user.id] = game
        // TODO update all games list and propagate this change via websockets to all waiting players
        availableId++

        return true
    }

    override fun isItUsersTurn(user: User, gameLobby: GameLobby): Boolean {
        val userSeed = if (user == gameLobby.owner) gameLobby.ownerSeed else gameLobby.opponentSeed
        return gameLobby.game?.currentSeed == userSeed
    }

    override fun addUserToAGame(user: User, gameId: Int): Boolean {
        if (userToGames.containsKey(user.id)) return false
        val game = games[gameId]
        game?.let {
            it.opponent = user
            userToGames[user.id] = game
            // TODO notify that user has been added to the game
            return true
        }
        return false
    }

    override fun startGame(gameLobby: GameLobby): Boolean {
        if (gameLobby.start()) {
            // TODO notify opponent that the game has started
            //
            return true
        }
        return false
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

    override fun removeUserFromAGame(user: User, gameId: Int): Boolean {
        val game = games[gameId]
        game?.let {
            userToGames.remove(user.id)
            if (game.opponent == user) {
                game.opponent = null
                // TODO notify that user has left
            } else if (game.owner == user) {
                // owner has left the game, remove the opponent
                userToGames.remove(game.opponent?.id)
                game.opponent = null

                // remove the game
                games.remove(gameId)

                // TODO notify that game has ended
            }
            return true
        }
        return false
    }

    override fun removeUserFromAGame(user: User): Boolean {
        val game = userToGames[user.id]
        game?.let {
            return removeUserFromAGame(user, game.id)
        }
        return false
    }

    override fun getGameByUser(user: User): GameLobby? = userToGames[user.id]

    override fun isUserInAGame(user: User) = userToGames.containsKey(user.id)

    override fun playGame() = "Game status: ${gameRepository.getGame()}"
}