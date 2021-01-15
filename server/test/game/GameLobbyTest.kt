package com.zcu.kiv.pia.tictactoe.game

import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.service.*
import com.zcu.kiv.pia.tictactoe.service.lobby.LobbyMessagingServiceImpl
import io.ktor.websocket.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GameLobbyTest {

    companion object {
        var staticLobbyService: LobbyService? = null
        val realtimeServiceMock = object : RealtimeService {
            override fun addConnection(connection: DefaultWebSocketServerSession, user: User?) {
            }

            override fun isConnected(user: User) = false

            override fun removeConnection(connection: DefaultWebSocketServerSession, user: User?) {
            }

            override fun receiveMessage(message: RealtimeMessage) {
            }

            override fun receiveMessage(message: String, connection: DefaultWebSocketServerSession) {
            }

            override fun sendMessage(message: RealtimeMessage, allUsers: Boolean, exclude: User?, vararg users: User) {
            }

            override fun addConnectionStatusListener(listener: RealtimeService.ConnectionStatusListener) {
            }

            override fun removeConnectionStatusListener(listener: RealtimeService.ConnectionStatusListener) {
            }

            override fun removeConnection(user: User) {

            }
        }

        @BeforeAll
        @JvmStatic
        fun setUp() {
            staticLobbyService = LobbyServiceImpl(
                LobbyMessagingServiceImpl(realtimeServiceMock),
                NotificationServiceImpl(realtimeServiceMock)
            )
        }
    }

    val lobbyService = staticLobbyService!!

    @Test
    fun `user should be able to create just one lobby`() {
        val user1 = User(1, "foo@bar.cz", "foo")

        lobbyService.createLobby(user1, 3, 3)
        assertThrows<LobbyService.UserAlreadyPresentInALobby> { lobbyService.createLobby(user1, 3, 3) }
    }

    @Test
    fun `user should be able to join a lobby once invited`() {
        val owner = User(1, "foo@bar.cz", "foo")
        val user = User(2, "foo@bar.cz", "foo")

        lobbyService.createLobby(owner, 10, 3)
        val lobby = lobbyService.getLobby(owner)!!

        assertThrows<LobbyService.UserNotInvited> { lobbyService.acceptInvite(lobby, user) }

        lobbyService.inviteUser(user, lobby, owner)
        lobbyService.acceptInvite(lobby, user)

        assertEquals(lobby.opponent!!, user)
    }

    @Test
    fun `lobbies should have unique ids`() {
        val user1 = User(1, "", "")
        val user2 = User(2, "", "")

        lobbyService.createLobby(user1, 1, 1)
        lobbyService.createLobby(user2, 1, 1)

        assertNotEquals(lobbyService.getLobby(user1)!!, lobbyService.getLobby(user2)!!)
    }
}