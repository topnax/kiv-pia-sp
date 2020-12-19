package com.zcu.kiv.pia.tictactoe

import com.fasterxml.jackson.databind.SerializationFeature
import com.zcu.kiv.pia.tictactoe.authentication.JwtConfig
import com.zcu.kiv.pia.tictactoe.authentication.UserCredential
import com.zcu.kiv.pia.tictactoe.authentication.UserPrincipal
import com.zcu.kiv.pia.tictactoe.controller.gameRoutes
import com.zcu.kiv.pia.tictactoe.controller.loginRoutes
import com.zcu.kiv.pia.tictactoe.database.DatabaseFactory
import com.zcu.kiv.pia.tictactoe.modules.mainModules
import com.zcu.kiv.pia.tictactoe.repository.DatabaseUserRepository
import com.zcu.kiv.pia.tictactoe.repository.UserRepository
import com.zcu.kiv.pia.tictactoe.service.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import mu.KotlinLogging
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(DefaultHeaders)
    install(CallLogging)

    install(Koin) {
        modules(
            mainModules
        )
    }


    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    if (!testing) {
        logger.debug { "not testing" }
        DatabaseFactory.init()

        val jwtIssuer = environment.config.property("jwt.domain").getString()
        val jwtSecret = environment.config.property("jwt.secret").getString()
        val jwtAudience = environment.config.property("jwt.audience").getString()
        val jwtRealm = environment.config.property("jwt.realm").getString()

        val jvtConfig = JwtConfig(jwtIssuer, jwtSecret, 10 * 60)

        install(Authentication) {
            jwt("jwt-auth") {
                verifier(jvtConfig.verifier)
                realm = jwtRealm
                validate {
                    with(it.payload) {
                        val login = getClaim("username").isNull
                        val id = getClaim("info").isNull
                        if (login || id)
                            null
                        else
                            JWTPrincipal(it.payload)
                    }
                }
            }
        }

        routing {

            loginRoutes(jvtConfig)

            authenticate("jwt-auth") {
                get("/secret") {
                    call.respondText("Hello from secret", contentType = ContentType.Text.Plain)
                    logger.debug {
                        "got username ${
                            call.principal<JWTPrincipal>()?.payload?.getClaim("username")?.asString()
                        }"
                    }
                }
            }
        }
    } else {
        logger.debug { "testing!!!" }
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
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }


    }

}


