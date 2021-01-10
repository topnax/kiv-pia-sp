package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.request.game.PlayGameRequest
import com.zcu.kiv.pia.tictactoe.service.GameService
import com.zcu.kiv.pia.tictactoe.service.LobbyService
import com.zcu.kiv.pia.tictactoe.service.UserService
import com.zcu.kiv.pia.tictactoe.utils.errorResponse
import com.zcu.kiv.pia.tictactoe.utils.getLoggedUser
import com.zcu.kiv.pia.tictactoe.utils.jwtAuthenticatedRoute
import com.zcu.kiv.pia.tictactoe.utils.successResponse
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import mu.KotlinLogging
import org.koin.ktor.ext.inject

private val logger = KotlinLogging.logger {}
// TODO invalid JSON?
fun Route.gameRoutes() {
    val gameService: GameService by inject()
    val lobbyService: LobbyService by inject()
    val userService: UserService by inject()

    jwtAuthenticatedRoute("game") {
        post("/refresh") {
            val user = getLoggedUser()
            lobbyService.sendUserState(user)
            successResponse()
        }
        post("/play") {
            val request = call.receive<PlayGameRequest>()
            val user = getLoggedUser()
            tryRun {

                val game = gameService.getGame(user)

                if (gameService.isItUsersTurn(user, game)) {
                    gameService.placeSeed(user, request.row, request.column, game)
                } else {
                    errorResponse("It is not user's turn!")
                }
            }
        }

        post("/leave") {
           val user = getLoggedUser()

            tryRun {
                val wrapper = gameService.getGame(user)

                gameService.removeUser(user, wrapper)
            }
        }
    }
}