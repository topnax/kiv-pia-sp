package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.authentication.JwtConfig
import com.zcu.kiv.pia.tictactoe.authentication.Token
import com.zcu.kiv.pia.tictactoe.authentication.UserCredential
import com.zcu.kiv.pia.tictactoe.authentication.UserPrincipal
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.model.response.DataResponse
import com.zcu.kiv.pia.tictactoe.model.response.ErrorResponse
import com.zcu.kiv.pia.tictactoe.model.response.SuccessResponse
import com.zcu.kiv.pia.tictactoe.service.UserService
import com.zcu.kiv.pia.tictactoe.service.HashService
import com.zcu.kiv.pia.tictactoe.utils.PasswordRuleVerifier
import io.ktor.application.*
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
            userService.addLoggedInUser(User(user.id, user.email))
            call.respond(DataResponse(Token(jvtConfig.makeToken(UserPrincipal(user.id, user.email)))))
            logger.debug {
                "LoggedIn users: ${userService.getLoggedInUsers().joinToString(separator = "\n") { it.email }}"
            }
        }
    }

    post("/register") {
        val credentials = call.receive<UserCredential>()

        with(PasswordRuleVerifier.verifyPassword(credentials.password).joinToString(separator = "\n") {
            it.violationMessage
        }) {
            when {
                isNotEmpty() -> {
                    call.respond(ErrorResponse(this))
                }
                !credentials.email.isEmail() -> {
                    call.respond(ErrorResponse("Email not in valid format"))
                }
                userService.getUserByEmail(credentials.email) != null -> {
                    call.respond(ErrorResponse("Email already in use"))
                }
                else -> {
                    userService.addUser(credentials.email, hashService.hashPassword(credentials.password))
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

fun String.isEmail() : Boolean {
    return emailRegex.matcher(this).matches()
}