package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.model.response.ErrorResponse
import com.zcu.kiv.pia.tictactoe.model.response.SuccessResponse
import com.zcu.kiv.pia.tictactoe.request.game.CreateGameRequest
import com.zcu.kiv.pia.tictactoe.request.game.LeaveGameRequest
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
    route("/api/game") {
        get("/play") {
            logger.debug { "Play endpoint invoked" }
            call.respondText(service.playGame(), contentType = ContentType.Text.Plain)
        }
    }

    post("/create") {
        val request = call.receive<CreateGameRequest>()

        val email = call.principal<JWTPrincipal>()?.payload?.getClaim("email")?.asString()!!
        val id = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!

        val user = User(id, email)

        if (service.createGame(user, request.boardSize)) {
            call.respond(SuccessResponse())
        } else {
            call.respond(ErrorResponse("User has already created a game"))
        }
    }

    post("/leave") {
        val request = call.receive<LeaveGameRequest>()

        val email = call.principal<JWTPrincipal>()?.payload?.getClaim("email")?.asString()!!
        val id = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()!!

        val user = User(id, email)

        if (service.removeUserFromAGame(user)) {
            call.respond(SuccessResponse())
        } else {
            call.respond(ErrorResponse("User not present in a game"))
        }
    }
}