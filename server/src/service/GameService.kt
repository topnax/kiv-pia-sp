package com.zcu.kiv.pia.tictactoe.service

import com.zcu.kiv.pia.tictactoe.database.logger
import com.zcu.kiv.pia.tictactoe.game.TicTacToeGame
import com.zcu.kiv.pia.tictactoe.model.GameLobby
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.model.response.*
import java.lang.Exception

interface GameService {
    fun playGame(): String

    @Throws(GameServiceException::class)
    fun inviteUser(user: User, gameLobby: GameLobby)

    fun createGame(user: User, boardSize: Int, victoriousCells: Int): Boolean

    fun addUserToLobby(user: User, lobbyId: Int): Boolean

    fun isItUsersTurn(user: User, gameLobby: GameLobby): Boolean

    fun removeUserFromAGame(user: User, gameId: Int): Boolean

    fun removeUserFromAGame(user: User): Boolean

    fun isUserInAGame(user: User): Boolean

    fun getGameState(user: User): TicTacToeGame?

    fun placeSeed(user: User, row: Int, column: Int): Boolean

    fun startGame(gameLobby: GameLobby): Boolean

    fun getGameLobby(user: User): GameLobby?

    fun getGameLobby(id: Int): GameLobby?

    @Throws(GameServiceException::class)
    fun acceptGameInvite(lobbyId: Int, user: User)

    @Throws(GameServiceException::class)
    fun declineGameInvite(lobbyId: Int, user: User)

    fun getGameInvites(user: User): List<Pair<String, Int>>

    class GameServiceException(val reason: String) : Exception(reason)
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

    override fun inviteUser(user: User, gameLobby: GameLobby) {
        if (gameLobby.opponent != null) throw GameService.GameServiceException("This game lobby is already full.")

        if (userToGames.containsKey(user.id)) throw GameService.GameServiceException("User ${user.username} already participates in a game")

        if (user == gameLobby.owner) throw GameService.GameServiceException("Owner of a lobby cannot invite itself")

        if (gameInvites[user]?.contains(gameLobby) == true) throw GameService.GameServiceException("User already invited to this lobby")

        // check whether no invites were sent to this user yet
        val invitationsToGames = gameInvites.getOrDefault(user, mutableListOf())

        invitationsToGames.add(gameLobby)

        gameLobby.invitedUsers.add(user)

        gameInvites[user] = invitationsToGames

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

        listOf(gameLobby.owner, user ).forEach {
            realtimeService.sendMessage(
                RealtimeMessage(
                    RealtimeMessage.Namespace.GAME,
                    "setState",
                    GameStateResponse(
                        GameStateResponse.StateType.PENDING,
                        PendingGameStateResponse(
                            gameLobby,
                            it == gameLobby.owner,
                            gameLobby.invitedUsers
                        )
                    )
                ),
                users = arrayOf(gameLobby.owner)
            )
        }
    }

    override fun acceptGameInvite(lobbyId: Int, user: User) {
        games[lobbyId]?.let { lobby ->
            val invited = gameInvites[user]?.contains(lobby) ?: false

            if (!invited) {
                throw GameService.GameServiceException("User ${user.username} not invited to the lobby")
            }
            if (addUserToLobby(user, lobbyId)) {
                gameInvites[user]?.remove(lobby)
                lobby.invitedUsers.remove(user)
            } else {
                throw GameService.GameServiceException("Could not add user to the lobby")
            }
        } ?: run {
            throw GameService.GameServiceException("Lobby not found")
        }
    }

    override fun declineGameInvite(lobbyId: Int, user: User) {
        games[lobbyId]?.let { lobby ->
            if (!lobby.invitedUsers.contains(user)) {
                throw GameService.GameServiceException("Cannot cancel an invite because user is not invited to the given lobby")
            }
            gameInvites[user]?.remove(lobby)
            lobby.invitedUsers.remove(user)
            realtimeService.sendMessage(
                RealtimeMessage(
                    RealtimeMessage.Namespace.NEWGAME,
                    "inviteGone",
                    InviteGoneResponse(
                        lobby.id
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
                            lobby,
                            true,
                            lobby.invitedUsers
                        )
                    )
                ),
                users = arrayOf(lobby.owner)
            )
            realtimeService.sendMessage(
                RealtimeMessage(
                    RealtimeMessage.Namespace.NOTIFICATIONS,
                    "new",
                    NotificationResponse("User ${user.username} has declined the invite :(")
                ),
                users = arrayOf(lobby.owner)
            )
        } ?: run {
            throw GameService.GameServiceException("Lobby not found")
        }
    }

    override fun getGameInvites(user: User): List<Pair<String, Int>> {
        logger.debug { "finding invites for $user ${gameInvites.get(user)?.size}" }
        val mapped = gameInvites.getOrDefault(user, mutableListOf()).map { gameLobby ->
            Pair(gameLobby.owner.username, gameLobby.id)
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

    override fun addUserToLobby(user: User, lobbyId: Int): Boolean {
        if (userToGames.containsKey(user.id)) return false
        val lobby = games[lobbyId]
        lobby?.let {
            it.opponent = user
            userToGames[user.id] = lobby

            val message = RealtimeMessage(
                RealtimeMessage.Namespace.GAME,
                "setState",
                GameStateResponse(
                    GameStateResponse.StateType.PENDING,
                    PendingGameStateResponse(lobby, true)
                )
            )

            realtimeService.sendMessage(
                message,
                users = arrayOf(lobby.owner, user)
            )

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
        games[gameId]?.let { lobby ->

            if (lobby.game != null) {
                userToGames.remove(user.id)
                if (lobby.opponent == user) {
                    lobby.opponent = null
                    // TODO notify that user has left
                } else if (lobby.owner == user) {
                    // owner has left the game, remove the opponent
                    userToGames.remove(lobby.opponent?.id)
                    lobby.opponent = null

                    // remove the game
                    games.remove(gameId)

                    // TODO notify that game has ended
                }
            } else {
                // game has not been started yet
                userToGames.remove(user.id)
                if (user == lobby.owner) {
                    // owner has left
                    // TODO remove pending invitations
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
                    lobby.invitedUsers.forEach { invitedUser ->
                        // remove pending invites
                        gameInvites[invitedUser]?.remove(lobby)
                    }

                    realtimeService.sendMessage(
                        RealtimeMessage(
                            RealtimeMessage.Namespace.NEWGAME,
                            "inviteGone",
                            InviteGoneResponse(
                                lobby.id
                            )
                        ),
                        users = lobby.invitedUsers.toTypedArray()
                    )
                } else if (user == lobby.opponent) {
                    lobby.opponent = null
                    // opponent has left
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
                    realtimeService.sendMessage(
                        RealtimeMessage(
                            RealtimeMessage.Namespace.GAME,
                            "setState",
                            GameStateResponse(
                                GameStateResponse.StateType.PENDING,
                                lobby
                            )
                        ),
                        users = arrayOf(lobby.owner)
                    )
                } else {
                    logger.error { "Unhandled lobby leave" }
                    return false
                }
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