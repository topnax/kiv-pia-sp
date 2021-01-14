package com.zcu.kiv.pia.tictactoe

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.zcu.kiv.pia.tictactoe.authentication.JwtConfig
import com.zcu.kiv.pia.tictactoe.controller.*
import com.zcu.kiv.pia.tictactoe.database.DatabaseFactory
import com.zcu.kiv.pia.tictactoe.module.mainModule
import com.zcu.kiv.pia.tictactoe.service.ConfigurationService
import com.zcu.kiv.pia.tictactoe.service.ConfigurationServiceImpl
import com.zcu.kiv.pia.tictactoe.service.HashService
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
import org.koin.dsl.module
import org.koin.ktor.ext.inject

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
            logger.info { "Got MissingKotlinParameterException: ${cause.message}" }
            call.respond(HttpStatusCode.BadRequest)
        }
        // TODO invalid json
    }
    install(Koin) {
        modules(
            mainModule + module {
                single<ConfigurationService> { ConfigurationServiceImpl(environment) }
            }
        )
    }

    install(CORS) {
        method(HttpMethod.Options)
        header(HttpHeaders.XForwardedProto)

        // TODO change based on your environment
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

        val hashService: HashService by inject()
        DatabaseFactory.init(hashService)
        val configurationService: ConfigurationService by inject()
        val jwtConfig = JwtConfig(configurationService.jwtIssuer, configurationService.jwtSecret, 10 * 60)

        install(Authentication) {
            jwt(JWT_AUTH_NAME) {
                verifier(jwtConfig.verifier)
                realm = configurationService.jwtRealm
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
                userAdministrationRoutes()
                gameHistoryRoutes()
                lobbyRoutes()

                loginRoutes(jwtConfig)

                userProfileRoutes()

                friendRoutes()

                websocketRoutes()
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
}
