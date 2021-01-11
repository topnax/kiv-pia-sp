package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.service.game.GameResultsService
import com.zcu.kiv.pia.tictactoe.utils.dataResponse
import com.zcu.kiv.pia.tictactoe.utils.errorResponse
import com.zcu.kiv.pia.tictactoe.utils.getLoggedUser
import com.zcu.kiv.pia.tictactoe.utils.jwtAuthenticatedRoute
import io.ktor.application.*
import io.ktor.routing.*
import mu.KotlinLogging
import org.koin.ktor.ext.inject

private val logger = KotlinLogging.logger {}

fun Route.gameHistoryRoutes() {
    val gameResultsService: GameResultsService by inject()

    jwtAuthenticatedRoute("/game") {
        route("/history") {
            get {
                val user = getLoggedUser()
                dataResponse(gameResultsService.getGameResultsByUser(user))
            }
            get("/turns/{id}") {
                val user = getLoggedUser()
                call.parameters["id"]?.toIntOrNull()?.let { gameResultId ->
                    gameResultsService.getGameResultById(gameResultId)?.let { result ->
                        if (result.crossUserId == user.id || result.noughtUserId == user.id) {
                            dataResponse(gameResultsService.getTurnsByGameResultId(gameResultId))
                        } else {
                            errorResponse("You did not participate in this game!")
                        }
                    }
                } ?: run {
                    errorResponse("ID parameter not specified")
                }
            }
        }
    }
}