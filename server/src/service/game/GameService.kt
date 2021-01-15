package com.zcu.kiv.pia.tictactoe.service.game

import com.zcu.kiv.pia.tictactoe.game.Seed
import com.zcu.kiv.pia.tictactoe.game.TicTacToeGame
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.service.NotificationService
import com.zcu.kiv.pia.tictactoe.service.RealtimeService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.util.*

val gameLogger = KotlinLogging.logger { }

interface GameService {
    /**
     * Checks whether the given user is currently able to place a seed in the given game
     */
    fun isItUsersTurn(user: User, gameWrapper: GameWrapper): Boolean

    /**
     * Places a seed at the given position as the given user
     */
    fun placeSeed(user: User, row: Int, column: Int, gameWrapper: GameWrapper)

    /**
     * Creates a new game with the given users and game parameters
     */
    fun createGame(user1: User, user2: User, boardSize: Int, victoriousCells: Int)

    /**
     * Sends the user the state of the game he participates in via WS
     */
    fun sendUserState(user: User)

    /**
     * Removes an user from the game
     */
    fun removeUser(user: User, gameWrapper: GameWrapper)

    /**
     * Removes a game from the list of games
     */
    fun removeGame(gameWrapper: GameWrapper)

    /**
     * Checks whether the given user is currently participating in any game
     */
    fun isUserPlaying(user: User): Boolean

    /**
     * Returns a game the user participates in. Throws an exception when the user does not participate in any game
     */
    fun getGame(user: User): GameWrapper

    abstract class GameServiceException(val reason: String) : Exception(reason)

    class UserNotPresentInGame() : GameServiceException("User not present in a game!")
    class NotUsersTurn() : GameServiceException("It is not user's turn!")
    class CouldNotPlaceSeed() : GameServiceException("Could not place seed.")
}

class GameWrapper(val id: Int, val cross: User, val nought: User, val game: TicTacToeGame)

class GameServiceImpl(
    private val gameMessagingService: GameMessagingService,
    private val notificationService: NotificationService,
    private val realtimeService: RealtimeService,
    private val gameResultService: GameResultsService
) :
    GameService {

    var nextGameId = 0

    /**
     * User IDs to a game they participate in
     */
    private val userToGames = hashMapOf<User, GameWrapper>()

    /**
     * Game lobbied mapped by IDs
     */
    private val games = hashMapOf<Int, GameWrapper>()

    private val random = Random()

    override fun createGame(user1: User, user2: User, boardSize: Int, victoriousCells: Int) {
        val game = TicTacToeGame(boardSize, Seed.CROSS, victoriousCells)

        val firstUserCross = random.nextBoolean()

        val cross = if (firstUserCross) user1 else user2
        val nought = if (firstUserCross) user2 else user1

        val wrapper = GameWrapper(nextGameId, cross, nought, game)

        games[nextGameId] = wrapper
        userToGames[user1] = wrapper
        userToGames[user2] = wrapper

        nextGameId++

        gameMessagingService.sendGameState(wrapper, user1)
        gameMessagingService.sendGameState(wrapper, user2)
    }

    override fun sendUserState(user: User) {
        userToGames[user]?.let { wrapper ->
            gameMessagingService.sendGameState(wrapper, user)
        }
    }

    override fun isItUsersTurn(user: User, gameWrapper: GameWrapper): Boolean {
        val userSeed = if (user == gameWrapper.cross) Seed.CROSS else Seed.NOUGHT

        return if (gameWrapper.game.turns.size % 2 == 0) {
            userSeed == Seed.CROSS
        } else {
            userSeed == Seed.NOUGHT
        }
    }

    override fun placeSeed(user: User, row: Int, column: Int, gameWrapper: GameWrapper) {
        if (!isItUsersTurn(user, gameWrapper)) throw GameService.NotUsersTurn()

        val seed = if (user == gameWrapper.cross) Seed.CROSS else Seed.NOUGHT

        if (!gameWrapper.game.addSeed(row, column, seed)) {
            throw GameService.CouldNotPlaceSeed()
        }

        listOf(gameWrapper.cross, gameWrapper.nought).forEach {
            gameMessagingService.sendNewTurn(row, column, seed, it)
        }

        if (gameWrapper.game.state == TicTacToeGame.State.DRAW) {
            gameMessagingService.sendGameDraw(gameWrapper, gameWrapper.cross)
            gameMessagingService.sendGameDraw(gameWrapper, gameWrapper.nought)

            removeGame(gameWrapper)
        } else if (gameWrapper.game.state == TicTacToeGame.State.WON) {
            gameMessagingService.sendGameWon(gameWrapper, gameWrapper.cross)
            gameMessagingService.sendGameWon(gameWrapper, gameWrapper.nought)

            removeGame(gameWrapper)
        }

    }


    override fun removeUser(user: User, gameWrapper: GameWrapper) {
        gameWrapper.game.winner = if (gameWrapper.cross == user) Seed.NOUGHT else Seed.CROSS

        gameMessagingService.sendGameWon(
            gameWrapper,
            gameWrapper.nought
        )
        gameMessagingService.sendGameWon(
            gameWrapper,
            gameWrapper.cross
        )

        notificationService.sendNotification(
            "Opponent has surrendered",
            if (gameWrapper.cross == user) gameWrapper.nought else gameWrapper.cross
        )

        gameLogger.info { "Removing user ${user.username}..." }
        removeGame(gameWrapper)
    }

    override fun removeGame(gameWrapper: GameWrapper) {
        GlobalScope.launch {
            gameResultService.addGameResult(gameWrapper)
        }
        userToGames.remove(gameWrapper.cross)
        userToGames.remove(gameWrapper.nought)
        games.remove(gameWrapper.id)
        gameLogger.info { "Removing game..." }
    }

    override fun getGame(user: User) = userToGames[user] ?: run {
        throw GameService.UserNotPresentInGame()
    }

    override fun isUserPlaying(user: User) = userToGames.containsKey(user)
}