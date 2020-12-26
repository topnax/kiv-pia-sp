package com.zcu.kiv.pia.tictactoe.game

import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.service.GameRepository
import com.zcu.kiv.pia.tictactoe.service.GameService
import com.zcu.kiv.pia.tictactoe.service.GameServiceImpl
import org.junit.Test

class GameLobbyTest {
    @Test
    fun `user should be able to create just one game`(){
        val gameService = GameServiceImpl(GameRepository())

        val user1 = User(1, "foo@bar.cz", "foo")

        assert(gameService.createGame(user1, 10, 3))
        assert(!gameService.createGame(user1, 10, 3))
    }

    @Test
    fun `user should be able to join an existing game`(){
        val gameService = GameServiceImpl(GameRepository())

        val user1 = User(1, "foo@bar.cz", "foo")
        val user2 = User(2, "foo@bar.cz", "foo")

        assert(gameService.createGame(user1, 10, 3))
        assert(gameService.addUserToAGame(user2, 0))
        assert(!gameService.addUserToAGame(user2, 0))
        assert(!gameService.addUserToAGame(user1, 0))
    }
}