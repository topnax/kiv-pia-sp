package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.JWT_AUTH_NAME
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.model.response.ErrorResponse
import com.zcu.kiv.pia.tictactoe.model.response.SuccessResponse
import com.zcu.kiv.pia.tictactoe.request.ChangePasswordRequest
import com.zcu.kiv.pia.tictactoe.service.HashService
import com.zcu.kiv.pia.tictactoe.service.UserService
import com.zcu.kiv.pia.tictactoe.utils.PasswordRuleVerifier
import com.zcu.kiv.pia.tictactoe.utils.errorResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging
import org.koin.ktor.ext.inject

private val logger = KotlinLogging.logger {}

fun Route.userProfileRoutes() {
    val userService: UserService by inject()
    val hashService: HashService by inject()
    authenticate(JWT_AUTH_NAME) {
        post("/profile/changepassword") {
            val request = call.receive<ChangePasswordRequest>()
            val user = User.fromJWTToken(call.principal()!!)
            val email = user.email
            with(PasswordRuleVerifier.verifyPassword(request.password).joinToString(separator = "\n") {
                it.violationMessage
            })
            {
                when {
                    isNotEmpty() -> {
                        errorResponse(this)
                    }
                    email != request.email -> {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse("Cannot change the password of another user")
                        )
                    }

                    request.password != request.passwordConfirm -> {
                        errorResponse("Passwords do not match")
                    }

                    else -> {
                        userService.getUserByCredentials(email, hashService.hashPassword(request.currentPassword))
                            ?.let {
                                userService.changeUserPassword(it, hashService.hashPassword(request.password))
                                call.respond(
                                    SuccessResponse()
                                )
                            } ?: run {
                            errorResponse("Current password not correct")
                        }
                    }
                }
            }
        }
    }
}