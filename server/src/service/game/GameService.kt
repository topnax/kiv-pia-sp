package com.zcu.kiv.pia.tictactoe.service

import com.zcu.kiv.pia.tictactoe.game.Seed
import com.zcu.kiv.pia.tictactoe.game.TicTacToeGame
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.service.game.GameMessagingService
import mu.KotlinLogging
import java.util.*

val gameLogger = KotlinLogging.logger {  }

interface GameService {
    fun isItUsersTurn(user: User, gameWrapper: GameWrapper): Boolean

    fun placeSeed(user: User, row: Int, column: Int, gameWrapper: GameWrapper)

    fun createGame(user1: User, user2: User, boardSize: Int, victoriousCells: Int)

    fun sendUserState(user: User)

    fun removeUser(user: User, gameWrapper: GameWrapper)

    fun removeGame(gameWrapper: GameWrapper)

    fun isUserPlaying(user: User): Boolean

    fun getGame(user: User): GameWrapper

    abstract class GameServiceException(val reason: String) : Exception(reason)

    class UserNotPresentInGame() : GameServiceException("User not present in a game!")
    class NotUsersTurn() : GameServiceException("It is not user's turn!")
    class CouldNotPlaceSeed() : GameServiceException("Could not place seed.")
}

class GameRepository {
    fun getGame() = "Tic Tac Toe game played :)"
}

class GameWrapper(val id: Int, val cross: User, val nought: User, val game: TicTacToeGame)

class GameServiceImpl(
    private val gameRepository: GameRepository,
    private val gameMessagingService: GameMessagingService,
    private val notificationService: NotificationService,
    private val realtimeService: RealtimeService
) :
    GameService {

    init {
        realtimeService.addOnConnectionStartedListener(object : RealtimeService.ConnectionStatusListener {
            override fun onConnected(user: User) {
            }

            override fun onDisconnected(user: User) {
                userToGames[user]?.let {
                    removeUser(user, it)
                }
            }
        })
    }

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
        } else if (gameWrapper.game.state == TicTacToeGame.State.WON) {
            gameMessagingService.sendGameWon(gameWrapper, gameWrapper.cross)
            gameMessagingService.sendGameWon(gameWrapper, gameWrapper.nought)
        }

        removeGame(gameWrapper)
    }


    override fun removeUser(user: User, gameWrapper: GameWrapper) {
        gameWrapper.game.winner = if (gameWrapper.cross == user) Seed.NOUGHT else Seed.CROSS

        gameMessagingService.sendGameWon(
            gameWrapper,
            if (gameWrapper.game.winner == Seed.CROSS) gameWrapper.cross else gameWrapper.nought
        )

        removeGame(gameWrapper)
    }

    override fun removeGame(gameWrapper: GameWrapper) {
        userToGames.remove(gameWrapper.cross)
        userToGames.remove(gameWrapper.nought)
        games.remove(gameWrapper.id)
    }

    override fun getGame(user: User) = userToGames[user] ?: run {
        throw GameService.UserNotPresentInGame()
    }

    override fun isUserPlaying(user: User) = userToGames.containsKey(user)
}