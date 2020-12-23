package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.JWT_AUTH_NAME
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.model.response.ErrorResponse
import com.zcu.kiv.pia.tictactoe.model.response.SuccessResponse
import com.zcu.kiv.pia.tictactoe.request.game.CreateGameRequest
import com.zcu.kiv.pia.tictactoe.request.game.JoinGameRequest
import com.zcu.kiv.pia.tictactoe.request.game.PlayGameRequest
import com.zcu.kiv.pia.tictactoe.service.GameService
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging
import org.koin.ktor.ext.inject

private val logger = KotlinLogging.logger {}

fun Route.gameRoutes() {
    val service: GameService by inject()

    authenticate(JWT_AUTH_NAME) {
        route("/api/game") {
            get("/play") {
                logger.debug { "Play endpoint invoked" }
                call.respondText(service.playGame(), contentType = ContentType.Text.Plain)
            }
            post("/create") {
                val request = call.receive<CreateGameRequest>()
                val user = User.fromJWTToken(call.principal()!!)

                if (service.createGame(user, request.boardSize, request.victoriousCells)) {
                    call.respond(SuccessResponse())
                } else {
                    call.respond(ErrorResponse("User has already created a game"))
                }
            }

            post("join") {
                val user = User.fromJWTToken(call.principal()!!)
                val request = call.receive<JoinGameRequest>()

                if (service.addUserToAGame(user, request.gameId)) {
                    call.respond(SuccessResponse())
                } else {
                    call.respond(ErrorResponse("Could not join the game"))
                }
            }

            post("/start") {
                val user = User.fromJWTToken(call.principal()!!)

                service.getGameByUser(user)?.let {
                    if (it.owner != user) {
                        call.respond(ErrorResponse("Only the owner of the game can start the game"))
                    } else if (it.opponent == null) {
                        call.respond(ErrorResponse("No opponent present, cannot start the game."))
                    } else if (!it.start()) {
                        call.respond(ErrorResponse("Failed to start the game"))
                    } else {
                        call.respond(SuccessResponse())
                    }
                } ?: run {
                    call.respond("Current user is not present in a game lobby")
                }
            }

            post("/play") {
                val request = call.receive<PlayGameRequest>()
                val user = User.fromJWTToken(call.principal()!!)

                val game = service.getGameByUser(user)
                if (game == null) {
                    call.respond(ErrorResponse("User not present in any game"))
                } else if (service. isItUsersTurn(user, game)) {
                    call.respond(ErrorResponse("It is not this user's turn"))
                } else if (!service.placeSeed(user, request.row, request.column)) {
                    call.respond(ErrorResponse("Could not place the seed"))
                } else {
                    call.respond(SuccessResponse())
                }
            }

            post("/leave") {
                val principal = call.principal<JWTPrincipal>()
                logger.debug { "got principal" }
                val user = User.fromJWTToken(principal!!)
                logger.debug { "got past user" }
                if (service.removeUserFromAGame(user)) {
                    call.respond(SuccessResponse())
                } else {
                    call.respond(ErrorResponse("User not present in a game"))
                }
            }
        }
    }


}