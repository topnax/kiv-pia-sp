package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.JWT_AUTH_NAME
import com.zcu.kiv.pia.tictactoe.service.RealtimeService
import com.zcu.kiv.pia.tictactoe.utils.getLoggedUser
import io.ktor.auth.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import mu.KotlinLogging
import org.koin.ktor.ext.inject

private val logger = KotlinLogging.logger{}

fun Route.websocketRoutes() {
    val wsService: RealtimeService by inject()
        webSocket("/ws") {
//            val user = getLoggedUser()
//            wsService.addConnection(this, user)
            send(Frame.Text("Testing :)"))
            try {
                while (true) {
                    val frame = incoming.receive()
                    when (frame) {
                        is Frame.Text -> {
                            wsService.addMessage(frame.readText())
                            logger.info { "received \"${frame.readText()}\"" }
                        }
                        else -> {
                            logger.warn { "received non text frame" }
                        }
                    }
                }
            } catch (exception: ClosedReceiveChannelException) {
               logger.warn { "a connection disconnected from the websocket" }
            } finally {
                // wsService.removeConnection(this, user)
            }
        }
}

