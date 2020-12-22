package com.zcu.kiv.pia.tictactoe.service

import com.zcu.kiv.pia.tictactoe.model.Game
import com.zcu.kiv.pia.tictactoe.model.User

interface GameService {
    fun playGame(): String

    fun createGame(user: User, boardSize: Int): Boolean

    fun addUserToAGame(user: User, gameId: Int): Boolean

    fun removeUserFromAGame(user: User, gameId: Int): Boolean

    fun removeUserFromAGame(user: User): Boolean

    fun isUserInAGame(user: User): Boolean
}

class GameRepository {
    fun getGame() = "Tic Tac Toe game played :)"
}

class GameServiceImpl(private val gameRepository: GameRepository) : GameService {

    private var availableId = 0

    /**
     * User IDs to a game they participate in
     */
    private val userToGames = hashMapOf<Int, Game>()

    private val games = hashMapOf<Int, Game>()

    override fun createGame(user: User, boardSize: Int): Boolean {
        if (isUserInAGame(user)) return false

        val game = Game(availableId, user, boardSize)
        games[availableId] = game
        userToGames[user.id] = game
        // TODO update all games list and propagate this change via websockets to all waiting players
        availableId++

        return true
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

    override fun isUserInAGame(user: User) = userToGames.containsKey(user.id)

    override fun playGame() = "Game status: ${gameRepository.getGame()}"
}