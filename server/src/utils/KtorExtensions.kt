package com.zcu.kiv.pia.tictactoe.utils

import com.zcu.kiv.pia.tictactoe.JWT_AUTH_NAME
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.model.response.DataResponse
import com.zcu.kiv.pia.tictactoe.model.response.ErrorResponse
import com.zcu.kiv.pia.tictactoe.model.response.SuccessResponse
import com.zcu.kiv.pia.tictactoe.service.UserService
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*

fun PipelineContext<Unit, ApplicationCall>.getLoggedUser() = User.fromJWTToken(this.call.principal()!!)
suspend fun PipelineContext<Unit, ApplicationCall>.isUserAdmin(userService: UserService): Boolean =
    User.fromJWTToken(this.call.principal()!!).let {
        if (!it.admin) return false
        userService.getUserById(it.id)?.let { dbUser ->
            return dbUser.admin
        } ?: run {
            return false
        }
    }

fun DefaultWebSocketServerSession.getLoggedUser() = User.fromJWTToken(this.call.principal()!!)
suspend fun PipelineContext<Unit, ApplicationCall>.successResponse() = call.respond(SuccessResponse())
suspend fun PipelineContext<Unit, ApplicationCall>.errorResponse(message: String) = call.respond(ErrorResponse(message))
suspend fun PipelineContext<Unit, ApplicationCall>.dataResponse(any: Any) = call.respond(DataResponse(any))
fun Route.jwtAuthenticatedRoute(path: String, build: Route.() -> Unit) =
    authenticate(JWT_AUTH_NAME) { createRouteFromPath(path).apply(build) }