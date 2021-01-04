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

    // expections
    abstract class LobbyServiceException(val reason: String) : Exception(reason)

    class UserAlreadyPresentInALobby : LobbyServiceException("User is already present in a lobby")
    class UserAlreadyInvitedToThisLobby : LobbyServiceException("User already invited to this lobby")
    class CannotInviteOwner : LobbyServiceException("The owner of this lobby cannot be invited to this lobby")
    class LobbyFull : LobbyServiceException("Cannot invite a user to a full lobby")
    class UserNotInvited : LobbyServiceException("User not invited to this lobby")
    class UserNotPresentInALobby : LobbyServiceException("User not present in a lobby")
    class UserNotAnOwner : LobbyServiceException("Only the owner of this lobby can perform this action")
    class LobbyNotFull : LobbyServiceException("Lobby not not full")
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
        if (lobby.owner == user) throw throw LobbyService.CannotInviteOwner()

        // lobby owner cannot invite himself
        if (lobby.owner == initiator) throw throw LobbyService.UserNotAnOwner()

        // lobby mustn't be full
        if (lobby.isFull()) throw throw LobbyService.LobbyFull()

        // user mustn't already be present in a lobby
        if (usersToLobbies.containsKey(user)) throw LobbyService.UserAlreadyPresentInALobby()

        // user mustn't be already invited to this lobby
        if (lobby.invitedUsers.contains(user)) throw LobbyService.UserAlreadyInvitedToThisLobby()

        // add user to the invite list of the given lobby
        lobby.invitedUsers.add(user)

        // get the list of invites of the given user
        val userInvites = invites.getOrDefault(user, mutableListOf())

        // add the lobby to the list of lobbies the user is invited to
        userInvites.add(lobby)

        // send a notification to the invited user
        lobbyMessagingService.sendNewInviteNotification(lobby, user)

        // send a message containing the updated state of the lobby
        lobbyMessagingService.sendLobbyState(lobby, lobby.owner)
    }

    override fun createLobby(user: User, boardSize: Int, victoriousCells: Int) {
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

    override fun isUserInALobby(user: User) = usersToLobbies.containsKey(user)

    override fun startLobby(lobby: Lobby, user: User) {
        if (lobby.owner != user) throw LobbyService.UserNotAnOwner()

        if (lobby.opponent == null) throw LobbyService.LobbyNotFull()

        destroyLobby(lobby)
    }

    override fun getLobby(user: User) = usersToLobbies[user]

    override fun getLobby(lobbyId: Int) = lobbies[lobbyId]

    override fun acceptInvite(lobby: Lobby, user: User) {
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
        if (userInvites.contains(lobby)) throw LobbyService.UserNotInvited()

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
        Pair(it.owner.username, it.owner.id)
    }
}