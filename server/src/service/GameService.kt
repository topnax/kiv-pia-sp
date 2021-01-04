package com.zcu.kiv.pia.tictactoe.service

import com.zcu.kiv.pia.tictactoe.database.logger
import com.zcu.kiv.pia.tictactoe.game.TicTacToeGame
import com.zcu.kiv.pia.tictactoe.model.GameLobby
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.model.response.GameStateResponse
import com.zcu.kiv.pia.tictactoe.model.response.InviteGoneResponse
import com.zcu.kiv.pia.tictactoe.model.response.NewInviteResponse
import com.zcu.kiv.pia.tictactoe.model.response.PendingGameStateResponse
import java.lang.Exception

interface GameService {
    fun playGame(): String

    @Throws(InviteUserException::class)
    fun inviteUser(user: User, gameLobby: GameLobby)

    fun createGame(user: User, boardSize: Int, victoriousCells: Int): Boolean

    fun addUserToAGame(user: User, gameId: Int): Boolean

    fun isItUsersTurn(user: User, gameLobby: GameLobby): Boolean

    fun removeUserFromAGame(user: User, gameId: Int): Boolean

    fun removeUserFromAGame(user: User): Boolean

    fun isUserInAGame(user: User): Boolean

    fun getGameState(user: User): TicTacToeGame?

    fun placeSeed(user: User, row: Int, column: Int): Boolean

    fun startGame(gameLobby: GameLobby): Boolean

    fun getGameLobby(user: User): GameLobby?

    fun getGameLobby(id: Int): GameLobby?

    fun getGameInvites(user: User): List<Pair<String, Int>>

    class InviteUserException(val reason: String) : Exception(reason)
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
    private var gameInvites = mutableMapOf<User, MutableList<User>>()

    /**
     * User IDs to a game they participate in
     */
    private val userToGames = hashMapOf<Int, GameLobby>()

    /**
     * Game lobbied mapped by IDs
     */
    private val games = hashMapOf<Int, GameLobby>()

    override fun inviteUser(user: User, gameLobby: GameLobby) {
        if (gameLobby.opponent != null) throw GameService.InviteUserException("This game lobby is already full.")

        if (userToGames.containsKey(user.id)) throw GameService.InviteUserException("User ${user.username} already participates in a game")

        if (user == gameLobby.owner) throw GameService.InviteUserException("Owner of a lobby cannot invite itself")

        if (gameInvites[user]?.contains(gameLobby.owner) == true) throw GameService.InviteUserException("User already invited to this lobby")

        // check whether no invites were sent to this user yet
        val usersThatInvited = gameInvites.getOrDefault(user, mutableListOf())

        usersThatInvited.add(gameLobby.owner)

        gameLobby.invitedUsers.add(user)

        gameInvites[user] = usersThatInvited

        logger.debug { "added $user to invited users" }

        realtimeService.sendMessage(
            RealtimeMessage(
                RealtimeMessage.Namespace.NEWGAME,
                "newInvite",
                NewInviteResponse(
                    gameLobby.id,
                    gameLobby.owner.username
                )
            ),
            users = arrayOf(user)
        )

        realtimeService.sendMessage(
            RealtimeMessage(
                RealtimeMessage.Namespace.GAME,
                "setState",
                GameStateResponse(
                    GameStateResponse.StateType.PENDING,
                    PendingGameStateResponse(
                        gameLobby,
                        true,
                        usersThatInvited
                    )
                )
            ),
            users = arrayOf(gameLobby.owner)
        )
    }

    override fun getGameInvites(user: User): List<Pair<String, Int>> {
        logger.debug { "finding invites for $user ${gameInvites.get(user)?.size}" }
        val mapped = gameInvites.getOrDefault(user, mutableListOf()).mapNotNull { userThatInvited ->
            logger.debug { "Found an invite ${userThatInvited.username} ${userThatInvited.id}, userTOGames ${userToGames[userThatInvited.id]?.id}" }
            userToGames[userThatInvited.id]?.let { lobby ->
                return@mapNotNull Pair(userThatInvited.username, lobby.id)
            }
        }.toList()
        logger.debug { "mapped ${mapped.size}" }
        return mapped

    }

    override fun createGame(user: User, boardSize: Int, victoriousCells: Int): Boolean {
        if (isUserInAGame(user)) return false

        val lobby = GameLobby(availableId, user, boardSize, victoriousCells)
        games[availableId] = lobby
        userToGames[user.id] = lobby
        // TODO update all games list and propagate this change via websockets to all waiting players
        realtimeService.sendMessage(
            RealtimeMessage(
                RealtimeMessage.Namespace.GAME,
                "setState",
                GameStateResponse(
                    GameStateResponse.StateType.PENDING,
                    PendingGameStateResponse(lobby)
                )
            ),
            users = arrayOf(user)
        )
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

            if (game.game != null) {
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
            } else if (user == game.owner) {
                // TODO remove pending invitations
                userToGames.remove(user.id)
                realtimeService.sendMessage(
                    RealtimeMessage(
                        RealtimeMessage.Namespace.GAME,
                        "setState",
                        GameStateResponse(
                            GameStateResponse.StateType.NONE,
                            null
                        )
                    ),
                    users = arrayOf(user)
                )

                // iterate over users invited to this lobby
                game.invitedUsers.forEach {  invitedUser ->
                    // remove pending invites
                    gameInvites[invitedUser]?.remove(game.owner)
                }

                realtimeService.sendMessage(
                    RealtimeMessage(
                        RealtimeMessage.Namespace.NEWGAME,
                        "inviteGone",
                        InviteGoneResponse(
                            game.id
                        )
                    ),
                    users = game.invitedUsers.toTypedArray()
                )
            }
            return true
        }
        return false
    }

    override fun getGameState(user: User): TicTacToeGame? = games[user.id]?.game

    override fun removeUserFromAGame(user: User): Boolean {
        val game = userToGames[user.id]
        game?.let {
            return removeUserFromAGame(user, game.id)
        }
        return false
    }

    override fun getGameLobby(user: User): GameLobby? = userToGames[user.id]

    override fun getGameLobby(id: Int): GameLobby? = games[id]

    override fun isUserInAGame(user: User) = userToGames.containsKey(user.id)

    override fun playGame() = "Game status: ${gameRepository.getGame()}"
}