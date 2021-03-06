package com.zcu.kiv.pia.tictactoe.service

import com.zcu.kiv.pia.tictactoe.model.Lobby
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.service.lobby.LobbyMessagingService
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

interface LobbyService {
    fun inviteUser(user: User, lobby: Lobby, initiator: User)

    fun createLobby(user: User, boardSize: Int, victoriousCells: Int)

    fun isUserInALobby(user: User): Boolean

    fun startLobby(lobby: Lobby, user: User)

    fun leaveLobby(lobby: Lobby, user: User)

    fun getLobby(user: User): Lobby?

    fun getLobby(lobbyId: Int): Lobby?

    fun acceptInvite(lobby: Lobby, user: User)

    fun declineInvite(lobby: Lobby, user: User)

    fun getInvites(user: User): List<Pair<String, Int>>

    fun sendUserState(user: User)

    // expections
    abstract class LobbyServiceException(val reason: String) : Exception(reason)

    class UserAlreadyPresentInALobby : LobbyServiceException("User is already present in a lobby")
    class UserAlreadyInvitedToThisLobby : LobbyServiceException("User already invited to this lobby")
    class CannotInviteOwner : LobbyServiceException("The owner of this lobby cannot be invited to this lobby")
    class LobbyFull : LobbyServiceException("Lobby is already full")
    class UserNotInvited : LobbyServiceException("User not invited to this lobby")
    class UserNotPresentInALobby : LobbyServiceException("User not present in a lobby")
    class UserNotAnOwner : LobbyServiceException("Only the owner of this lobby can perform this action")
    class LobbyNotFull : LobbyServiceException("Lobby not not full")
    class InvalidLobbyParameters(message: String) : LobbyServiceException(message)
}

class LobbyServiceImpl(
    private val lobbyMessagingService: LobbyMessagingService,
    private val notificationService: NotificationService
) : LobbyService {

    /**
     * The ID of the next lobby to be created
     */
    private var nextLobbyId = 0

    /**
     * List of active lobbies
     */
    private val lobbies = mutableMapOf<Int, Lobby>()

    /**
     * A map where users are being mapped to list of lobbies to which they are invited to
     */
    private val invites = mutableMapOf<User, MutableList<Lobby>>()

    /**
     * A map where users are mapped to lobbies they participate in
     */
    private val usersToLobbies = mutableMapOf<User, Lobby>()

    override fun inviteUser(user: User, lobby: Lobby, initiator: User) {
        // lobby owner cannot invite himself
        if (lobby.owner != initiator) throw LobbyService.UserNotAnOwner()

        // lobby owner cannot invite himself
        if (lobby.owner == user) throw LobbyService.CannotInviteOwner()

        // lobby mustn't be full
        if (lobby.isFull()) throw LobbyService.LobbyFull()

        // user mustn't already be present in a lobby
        if (usersToLobbies.containsKey(user)) throw LobbyService.UserAlreadyPresentInALobby()

        // user mustn't be already invited to this lobby
        if (lobby.invitedUsers.contains(user)) throw LobbyService.UserAlreadyInvitedToThisLobby()

        // add user to the invite list of the given lobby
        lobby.invitedUsers.add(user)

        // get the list of invites of the given user
        val userInvites = invites.getOrDefault(user, mutableListOf())
        invites[user] = userInvites

        // add the lobby to the list of lobbies the user is invited to
        userInvites.add(lobby)

        // send a notification to the invited user
        lobbyMessagingService.sendNewInviteNotification(lobby, user)

        // send a message containing the updated state of the lobby
        lobbyMessagingService.sendLobbyState(lobby, lobby.owner)
    }

    override fun createLobby(user: User, boardSize: Int, victoriousCells: Int) {
        validateLobbyParameters(boardSize, victoriousCells)
        // user mustn't already be in a lobby
        if (usersToLobbies.contains(user)) throw LobbyService.UserAlreadyPresentInALobby()

        // create a new lobby
        val lobby = Lobby(nextLobbyId++, user, boardSize, victoriousCells)

        // add the lobby to the list of lobbies
        lobbies[lobby.id] = lobby

        // assign a lobby to the owner user
        usersToLobbies[user] = lobby

        // send the new lobby state
        lobbyMessagingService.sendLobbyState(lobby, user)
    }

    private fun validateLobbyParameters(boardSize: Int, victoriousCells: Int) {
        when (boardSize) {
            3 -> {
                if (victoriousCells != 3) {
                    throw LobbyService.InvalidLobbyParameters("For board size of 3 the victorious cells option can only be set to 3.")
                }
            }
            5 -> {
                when (victoriousCells) {
                    3,5 -> {

                    }
                    else -> {
                        throw LobbyService.InvalidLobbyParameters("For board size of 5 the victorious cells option can only be set to 3 or 5.")
                    }
                }
            }
            10 -> {
                when (victoriousCells) {
                    3, 5, 10 -> {

                    }
                    else -> {
                        throw LobbyService.InvalidLobbyParameters("For board size of 10 the victorious cells option can only be set to 3, 5 or 10.")
                    }
                }
            }
            else -> throw LobbyService.InvalidLobbyParameters("Available board sizes are 3, 5 and 10")
        }
    }

    override fun isUserInALobby(user: User) = usersToLobbies.containsKey(user)

    override fun startLobby(lobby: Lobby, user: User) {
        if (lobby.owner != user) throw LobbyService.UserNotAnOwner()

        if (lobby.opponent == null) throw LobbyService.LobbyNotFull()

        destroyLobby(lobby)
    }

    override fun getLobby(user: User) = usersToLobbies[user]

    override fun getLobby(lobbyId: Int) = lobbies[lobbyId]

    override fun acceptInvite(lobby: Lobby, user: User) {
        // lobby mustn't be already full
        if (lobby.isFull()) throw LobbyService.LobbyFull()

        // user cannot already be present in a lobby
        if (usersToLobbies.containsKey(user)) throw LobbyService.UserAlreadyPresentInALobby()

        // user must be invited to the lobby in order to join it
        if (!lobby.invitedUsers.contains(user)) throw LobbyService.UserNotInvited()

        // assign the user to the lobby
        lobby.opponent = user
        usersToLobbies[user] = lobby

        // remove the invite
        invites[user]?.remove(lobby)

        // remove the user from the list of invited users
        lobby.invitedUsers.remove(user)

        // notify the owner
        lobbyMessagingService.sendLobbyState(lobby, lobby.owner)

        // notify the user
        lobbyMessagingService.sendLobbyState(lobby, user)
        lobbyMessagingService.sendGoneInviteNotification(lobby, user)
    }

    override fun declineInvite(lobby: Lobby, user: User) {
        val userInvites = invites.getOrDefault(user, mutableListOf())

        // user must be invited in order to be able to decline the invitation
        if (!userInvites.contains(lobby)) throw LobbyService.UserNotInvited()

        // remove the user from the invite list of the lobby
        lobby.invitedUsers.remove(user)

        // remove the invite from the invite list
        userInvites.remove(lobby)

        // notify the owner of the lobby that a user has declined an invitation
        lobbyMessagingService.sendLobbyState(lobby, lobby.owner)
        notificationService.sendNotification("User ${user.username} has declined an invitation.", lobby.owner)

        // notify the user that the invite is gone
        lobbyMessagingService.sendGoneInviteNotification(lobby, user)
    }

    override fun leaveLobby(lobby: Lobby, user: User) {
        if (!usersToLobbies.containsKey(user)) throw LobbyService.UserNotPresentInALobby()

        // remove from userToLobbies
        usersToLobbies.remove(user)

        when (user) {
            lobby.owner -> {
                // the owner is leaving the lobby - destroy it
                destroyLobby(lobby)
                lobby.opponent?.let {
                    notificationService.sendNotification("Owner left the lobby", it)
                }
            }
            lobby.opponent -> {
                lobby.opponent = null

                // notify the owner
                lobbyMessagingService.sendLobbyState(lobby, lobby.owner)
            }
            else -> {
                logger.error { "User to lobbies contains a user ${user.username} that does not belong to the given lobby ${lobby.id}" }
            }
        }
    }

    private fun destroyLobby(lobby: Lobby) {
        // remove users from usersToLobbies map
        usersToLobbies.remove(lobby.owner)
        usersToLobbies.remove(lobby.opponent)

        // notify opponent
        lobby.opponent?.let {
            lobbyMessagingService.sendLobbyDestroyedNotification(lobby, it)
        }

        // notify owner
        lobbyMessagingService.sendLobbyDestroyedNotification(lobby, lobby.owner)

        // notify and remove pending invites
        lobby.invitedUsers.forEach { invitedUser ->
            invites[invitedUser]?.remove(lobby)
            lobbyMessagingService.sendGoneInviteNotification(lobby, invitedUser)
        }

        // remove the lobby from the list
        lobbies.remove(lobby.id)
    }

    override fun getInvites(user: User) = invites.getOrDefault(user, mutableListOf()).map {
        Pair(it.owner.username, it.id)
    }

    override fun sendUserState(user: User) {
        usersToLobbies[user]?.let { lobby ->
            lobbyMessagingService.sendLobbyState(lobby, user)
        }
    }
}