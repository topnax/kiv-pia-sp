package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.authentication.JwtConfig
import com.zcu.kiv.pia.tictactoe.authentication.Token
import com.zcu.kiv.pia.tictactoe.authentication.UserCredential
import com.zcu.kiv.pia.tictactoe.authentication.UserPrincipal
import com.zcu.kiv.pia.tictactoe.model.response.DataResponse
import com.zcu.kiv.pia.tictactoe.model.response.ErrorResponse
import com.zcu.kiv.pia.tictactoe.model.response.SuccessResponse
import com.zcu.kiv.pia.tictactoe.repository.UserRepository
import com.zcu.kiv.pia.tictactoe.service.HashService
import com.zcu.kiv.pia.tictactoe.utils.PasswordRuleVerifier
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging
import org.koin.ktor.ext.inject
import java.util.regex.Pattern
import java.util.regex.Pattern.compile

private val logger = KotlinLogging.logger {}

fun Route.loginRoutes(jvtConfig: JwtConfig) {
    val userRepository: UserRepository by inject()
    val hashService: HashService by inject()

    post("/login") {
        val credentials = call.receive<UserCredential>()
        val user = userRepository.userByCredentials(
            credentials.email,
            hashService.hashPassword(credentials.password)
        )
        if (user == null) {
            call.respond(HttpStatusCode.Unauthorized)
        } else {
            call.respond(DataResponse(Token(jvtConfig.makeToken(UserPrincipal(user.email)))))
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
                userRepository.getUserByEmail(credentials.email) != null -> {
                    call.respond(ErrorResponse("Email already in use"))
                }
                else -> {
                    userRepository.addUser(credentials.email, hashService.hashPassword(credentials.password))
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