package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.request.ChangeUserPasswordRequest
import com.zcu.kiv.pia.tictactoe.request.ChangeUserRoleRequest
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
            if (isUserAdmin(userService)) {
                dataResponse(userService.getUsers())
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
        post("/changepassword") {
            val request = call.receive<ChangeUserPasswordRequest>()
            when {
                request.password.isEmpty() -> {
                    errorResponse("Password mustn't be empty")
                }
                isUserAdmin(userService) -> {
                    userService.getUserById(request.userId)?.let {
                        if (!it.admin || it.id == getLoggedUser().id) {
                            userService.changeUserPassword(it, hashService.hashPassword(request.password))
                            successResponse()
                        } else {
                            errorResponse("Cannot change other admin's password")
                        }
                    } ?: run {
                        errorResponse("User not found by ID")
                    }
                }
                else -> {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
        }

        post("/promote") {
            val request = call.receive<ChangeUserRoleRequest>()
            if (isUserAdmin(userService)) {
                userService.getUserById(request.userId)?.let {
                    if (it.username != "admin") {
                        userService.promoteUserToAdmin(it)
                        successResponse()
                    } else {
                        errorResponse("Cannot promote the 'admin' user.")
                    }
                } ?: run {
                    errorResponse("User not found by ID")
                }
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }

        post("/demote") {
            val request = call.receive<ChangeUserRoleRequest>()
            if (isUserAdmin(userService)) {
                userService.getUserById(request.userId)?.let {
                    if (it.username != "admin") {
                        userService.demoteAdminToUser(it)
                        successResponse()
                    } else {
                        errorResponse("Cannot promote the 'admin' user.")
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
