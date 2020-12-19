package com.zcu.kiv.pia.tictactoe

import com.fasterxml.jackson.databind.SerializationFeature
import com.zcu.kiv.pia.tictactoe.authentication.JwtConfig
import com.zcu.kiv.pia.tictactoe.authentication.UserCredential
import com.zcu.kiv.pia.tictactoe.authentication.UserPrincipal
import com.zcu.kiv.pia.tictactoe.controller.gameRoutes
import com.zcu.kiv.pia.tictactoe.modules.mainModule
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

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(DefaultHeaders)
    install(CallLogging)

    install(Koin) {
        modules(mainModule)
    }

    val jwtIssuer = environment.config.property("jwt.domain").getString()
    val jwtSecret = environment.config.property("jwt.secret").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()

    val jvtConfig = JwtConfig(jwtIssuer, jwtSecret, 10 * 60)

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

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

        post("/login") {
            with(Dispatchers.IO) {
                val credentials = call.receive<UserCredential>()
                if (credentials.username == "foo" && credentials.password == "bar") {
                    call.respond(jvtConfig.makeToken(UserPrincipal("foo", "This is foo")))
                } else if (credentials.username == "baz" && credentials.password == "boo") {
                    call.respond(jvtConfig.makeToken(UserPrincipal("baz", "This is bazingus")))
                } else call.respond(HttpStatusCode.Unauthorized)
            }
        }

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

}


