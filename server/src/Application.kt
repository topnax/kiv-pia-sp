package com.zcu.kiv.pia.tictactoe

import com.zcu.kiv.pia.tictactoe.controller.gameRoutes
import com.zcu.kiv.pia.tictactoe.modules.mainModule
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.Koin

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(DefaultHeaders)
    install(CallLogging)

    install(Koin) {
        modules(mainModule)
    }

//    install(io.ktor.websocket.WebSockets) {
//        pingPeriod = Duration.ofSeconds(15)
//        timeout = Duration.ofSeconds(15)
//        maxFrameSize = Long.MAX_VALUE
//        masking = false
//    }

    routing {
        gameRoutes()
        get("/") {
            call.respondText("Hello from root", contentType = ContentType.Text.Plain)
        }

    }
}

