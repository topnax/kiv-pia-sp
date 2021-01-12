package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.request.ChangeUserPasswordRequest
import com.zcu.kiv.pia.tictactoe.service.HashService
import com.zcu.kiv.pia.tictactoe.service.UserService
import com.zcu.kiv.pia.tictactoe.utils.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject

fun Route.userAdministrationRoutes() {
    jwtAuthenticatedRoute("/administration/users") {
        val userService: UserService by inject()
        val hashService: HashService by inject()
        get("/list") {
            val user = getLoggedUser()
            if (user.admin) {
                dataResponse(userService.getUsers())
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
        post("/changepassword") {
            val request = call.receive<ChangeUserPasswordRequest>()
            val user = getLoggedUser()
            if (request.password.isEmpty()) {
                errorResponse("Password mustn't be empty")
            } else if (user.admin) {
                userService.getUserById(request.userId)?.let {
                    if (!it.admin || it.id == user.id) {
                        userService.changeUserPassword(it, hashService.hashPassword(request.password))
                        successResponse()
                    } else {
                        errorResponse("Cannot change other admin's password")
                    }
                } ?: run {
                    errorResponse("User not found by ID")
                }
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}
