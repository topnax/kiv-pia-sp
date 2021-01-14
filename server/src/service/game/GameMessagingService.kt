package com.zcu.kiv.pia.tictactoe.service.game

import com.zcu.kiv.pia.tictactoe.game.Seed
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.model.response.GameDrawResponse
import com.zcu.kiv.pia.tictactoe.model.response.GameWonResponse
import com.zcu.kiv.pia.tictactoe.model.response.GameStateResponse
import com.zcu.kiv.pia.tictactoe.model.response.NewTurnResponse
import com.zcu.kiv.pia.tictactoe.service.RealtimeMessage
import com.zcu.kiv.pia.tictactoe.service.RealtimeService

interface GameMessagingService {
    fun sendGameState(gameWrapper: GameWrapper, user: User)
    fun sendNewTurn(row: Int, column: Int, seed: Seed, user: User)
    fun sendGameWon(gameWrapper: GameWrapper, user: User)
    fun sendGameDraw(gameWrapper: GameWrapper, user: User)
}

class GameMessagingServiceImpl(private val realtimeService: RealtimeService) : GameMessagingService {
    override fun sendGameState(gameWrapper: GameWrapper, user: User) {
        realtimeService.sendMessage(
            RealtimeMessage(
                RealtimeMessage.Namespace.GAME,
                "setState",
                GameStateResponse(
                    gameWrapper,
                    if (user == gameWrapper.cross) gameWrapper.nought else gameWrapper.cross
                ),
            ), users = arrayOf(user)
        )
    }

    override fun sendNewTurn(row: Int, column: Int, seed: Seed, user: User) {
        realtimeService.sendMessage(
            RealtimeMessage(
                RealtimeMessage.Namespace.GAME,
                "newTurn",
                NewTurnResponse(row, column, seed)
            ), users = arrayOf(user)
        )
    }

    override fun sendGameWon(gameWrapper: GameWrapper, user: User) {
        realtimeService.sendMessage(
            RealtimeMessage(
                RealtimeMessage.Namespace.GAME,
                "gameWon",
                GameWonResponse(gameWrapper.game)
            ), users = arrayOf(user)
        )
    }

    override fun sendGameDraw(gameWrapper: GameWrapper, user: User) {
        realtimeService.sendMessage(
            RealtimeMessage(
                RealtimeMessage.Namespace.GAME,
                "gameDraw",
                GameDrawResponse()
            ), users = arrayOf(user)
        )
    }
}