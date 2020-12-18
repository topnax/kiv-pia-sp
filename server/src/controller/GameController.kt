package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.service.GameService
import io.ktor.application.*
import io.ktor.http.*
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
}