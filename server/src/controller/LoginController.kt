package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.JWT_AUTH_NAME
import com.zcu.kiv.pia.tictactoe.authentication.JwtConfig
import com.zcu.kiv.pia.tictactoe.authentication.Token
import com.zcu.kiv.pia.tictactoe.authentication.UserCredential
import com.zcu.kiv.pia.tictactoe.authentication.UserPrincipal
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.model.response.DataResponse
import com.zcu.kiv.pia.tictactoe.model.response.ErrorResponse
import com.zcu.kiv.pia.tictactoe.model.response.SuccessResponse
import com.zcu.kiv.pia.tictactoe.request.game.RegisterRequest
import com.zcu.kiv.pia.tictactoe.service.UserService
import com.zcu.kiv.pia.tictactoe.service.HashService
import com.zcu.kiv.pia.tictactoe.service.RealtimeService
import com.zcu.kiv.pia.tictactoe.utils.PasswordRuleVerifier
import com.zcu.kiv.pia.tictactoe.utils.dataResponse
import com.zcu.kiv.pia.tictactoe.utils.errorResponse

import com.zcu.kiv.pia.tictactoe.utils.getLoggedUser
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging
import org.koin.ktor.ext.inject
import java.util.regex.Pattern.compile

private val logger = KotlinLogging.logger {}

fun Route.loginRoutes(jvtConfig: JwtConfig) {

    val hashService: HashService by inject()
    val userService: UserService by inject()
    val realtimeService: RealtimeService by inject()

    route("/auth") {
        authenticate(JWT_AUTH_NAME) {
            get("/user") {
                userService.getUserById(getLoggedUser().id)?.let {
                    dataResponse(it)
                } ?: run {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
            post("/logout") {
                val loggedUser = getLoggedUser()
                userService.removeLoggedInUser(User.fromJWTToken(call.principal()!!))
                realtimeService.removeConnection(loggedUser)
                call.respond(SuccessResponse())
            }
        }

        post("/login") {
            val credentials = call.receive<UserCredential>()

            // check whether use login credentials are valid
            val user = userService.getUserByCredentials(
                credentials.email,
                hashService.hashPassword(credentials.password)
            )
            if (user == null) {
                // user not found
                call.respond(HttpStatusCode.Unauthorized)
            } else {
                // user has logged in
//                userService.addLoggedInUser(user)
                call.respond(
                        Token(
                            jvtConfig.makeToken(
                                UserPrincipal(
                                    user.id,
                                    user.email,
                                    user.username,
                                    user.admin
                                )
                            )
                        )
                )
                logger.debug {
                    "LoggedIn users: ${userService.getLoggedInUsers().joinToString(separator = "\n") { it.email }}"
                }
            }
        }
    }

    post("/register") {
        val request = call.receive<RegisterRequest>()

        // verify password strength
        with(PasswordRuleVerifier.verifyPassword(request.password).joinToString(separator = "\n") {
            it.violationMessage
        }) {
            when {
                request.password != request.confirmPassword -> {
                    errorResponse("Passwords do not match")
                }
                isNotEmpty() -> {
                    call.respond(ErrorResponse(this))
                }
                !request.email.isEmail() -> {
                    call.respond(ErrorResponse("Email not in valid format"))
                }
                userService.getUserByEmail(request.email) != null -> {
                    call.respond(ErrorResponse("Email already in use"))
                }
                userService.getUserByUsername(request.username) != null -> {
                    call.respond(ErrorResponse("Username already in use"))
                }
                else -> {
                    userService.addUser(request.email, request.username, hashService.hashPassword(request.password))
                    call.respond(SuccessResponse())
                }
            }
        }
    }
}

private val emailRegex = compile(
    "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
)

fun String.isEmail(): Boolean {
    return emailRegex.matcher(this).matches()
}