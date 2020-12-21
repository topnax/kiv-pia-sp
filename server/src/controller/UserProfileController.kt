package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.JWT_AUTH_NAME
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.model.response.ErrorResponse
import com.zcu.kiv.pia.tictactoe.model.response.SuccessResponse
import com.zcu.kiv.pia.tictactoe.request.ChangePasswordRequest
import com.zcu.kiv.pia.tictactoe.service.HashService
import com.zcu.kiv.pia.tictactoe.service.UserService
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
            val email = call.principal<JWTPrincipal>()?.payload?.getClaim("email")?.asString()

            when {
                email == null -> {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Cannot change the password without being logged in")
                    )
                }

                email != request.email -> {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Cannot change the password of another user")
                    )
                }

                request.password != request.passwordConfirm -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Passwords do not match")
                    )
                }

                userService.getUserByCredentials(email, hashService.hashPassword(request.currentPassword)) == null -> {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Current password not correct")
                    )
                }

                else -> {
                    userService.changeUserPassword(User(email), hashService.hashPassword(request.password))
                    call.respond(
                        SuccessResponse()
                    )
                }
            }
        }
    }
}