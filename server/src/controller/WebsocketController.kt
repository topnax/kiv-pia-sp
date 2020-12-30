package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.authentication.JwtConfig
import io.ktor.routing.*
import io.ktor.websocket.*

fun Route.websocketRoutes() {
    webSocket("/ws") {
        val r = incoming.receive()
        this
    }
}
