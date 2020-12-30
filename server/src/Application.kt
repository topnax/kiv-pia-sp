package com.zcu.kiv.pia.tictactoe

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.zcu.kiv.pia.tictactoe.authentication.JwtConfig
import com.zcu.kiv.pia.tictactoe.controller.friendRoutes
import com.zcu.kiv.pia.tictactoe.controller.gameRoutes
import com.zcu.kiv.pia.tictactoe.controller.loginRoutes
import com.zcu.kiv.pia.tictactoe.controller.userProfileRoutes
import com.zcu.kiv.pia.tictactoe.database.DatabaseFactory
import com.zcu.kiv.pia.tictactoe.module.mainModule
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import mu.KotlinLogging
import org.koin.ktor.ext.Koin
import java.time.Duration

const val JWT_AUTH_NAME = "jwt-auth"
private val logger = KotlinLogging.logger {}

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(DefaultHeaders)
    install(CallLogging)

    // handle call handler exceptions
    install(StatusPages) {
        // JSON parser exception
        exception<MissingKotlinParameterException> { cause ->
            logger.info {"Got MissingKotlinParameterException: ${cause.message}"}
            call.respond(HttpStatusCode.BadRequest)
        }
        // TODO invalid json
    }

    install(Koin) {
        modules(
            mainModule
        )
    }

    install(CORS) {
        method(HttpMethod.Options)
        header(HttpHeaders.XForwardedProto)

        // TODO change
        anyHost()
        header("Authorization")
        allowCredentials = true
        allowNonSimpleContentTypes = true
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
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
            jwt(JWT_AUTH_NAME) {
                verifier(jvtConfig.verifier)
                realm = jwtRealm
                validate {
                    with(it.payload) {
                        val email = getClaim("email").isNull
                        val id = getClaim("id").isNull
                        val username = getClaim("username").isNull
                        if (email || id || username)
                            null
                        else
                            JWTPrincipal(it.payload)
                    }
                }
            }
        }

        routing {

            route("/api") {
                gameRoutes()

                loginRoutes(jvtConfig)

                userProfileRoutes()

                friendRoutes()
            }

            authenticate(JWT_AUTH_NAME) {
                get("/secret") {
                    call.respondText("Hello from secret", contentType = ContentType.Text.Plain)
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
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }


    }

}


