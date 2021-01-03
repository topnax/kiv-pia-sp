package com.zcu.kiv.pia.tictactoe.game

import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.service.*
import io.ktor.websocket.*
import org.junit.Test

class GameLobbyTest {

    val realtimeServiceMock = object : RealtimeService {
        override fun addConnection(connection: DefaultWebSocketServerSession, user: User?) {
        }

        override fun isConnected(user: User) = false

        override fun removeConnection(connection: DefaultWebSocketServerSession, user: User?) {
        }

        override fun addMessage(message: RealtimeMessage) {
        }

        override fun addMessage(message: String, connection: DefaultWebSocketServerSession) {
        }

        override fun sendMessage(message: RealtimeMessage, allUsers: Boolean, exclude: User?, vararg users: User) {
        }

        override fun addOnConnectionStartedListener(listener: RealtimeService.ConnectionStatusListener) {
        }

        override fun removeOnConnectionStartedListener(listener: RealtimeService.ConnectionStatusListener) {
        }

    }

    @Test
    fun `user should be able to create just one game`() {
        val gameService = GameServiceImpl(GameRepository(), realtimeServiceMock)

        val user1 = User(1, "foo@bar.cz", "foo")

        assert(gameService.createGame(user1, 10, 3))
        assert(!gameService.createGame(user1, 10, 3))
    }

    @Test
    fun `user should be able to join an existing game`() {
        val gameService = GameServiceImpl(GameRepository(), realtimeServiceMock)

        val user1 = User(1, "foo@bar.cz", "foo")
        val user2 = User(2, "foo@bar.cz", "foo")

        assert(gameService.createGame(user1, 10, 3))
        assert(gameService.addUserToAGame(user2, 0))
        assert(!gameService.addUserToAGame(user2, 0))
        assert(!gameService.addUserToAGame(user1, 0))
    }
}