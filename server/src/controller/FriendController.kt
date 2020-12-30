package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.JWT_AUTH_NAME
import com.zcu.kiv.pia.tictactoe.model.FriendRequest
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.model.response.DataResponse
import com.zcu.kiv.pia.tictactoe.model.response.ErrorResponse
import com.zcu.kiv.pia.tictactoe.model.response.FriendRequestResponse
import com.zcu.kiv.pia.tictactoe.model.response.SuccessResponse
import com.zcu.kiv.pia.tictactoe.request.AcceptFriendRequest
import com.zcu.kiv.pia.tictactoe.request.CancelFriendRequest
import com.zcu.kiv.pia.tictactoe.request.DeclineFriendRequest
import com.zcu.kiv.pia.tictactoe.request.NewFriendRequest
import com.zcu.kiv.pia.tictactoe.service.FriendService
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import org.koin.ktor.ext.inject

fun Route.friendRoutes() {
    val friendService: FriendService by inject()
    jwtAuthenticatedRoute("/friend") {
        post("/new") {
            val request = call.receive<NewFriendRequest>()
            val user = getLoggedUser()

            if (friendService.addFriendRequest(FriendRequest(user.id, request.userId))) {
                successResponse()
            } else {
                errorResponse("Could not create a new friend request")
            }
        }

        post("/accept") {
            val request = call.receive<AcceptFriendRequest>()
            val user = getLoggedUser()

            if (friendService.confirmFriendRequest(request.requestId, user.id)) {
                successResponse()
            } else {
                errorResponse("Could not confirm the friend request")
            }
        }

        post("/decline") {
            val request = call.receive<DeclineFriendRequest>()
            val user = getLoggedUser()

            if (friendService.declineFriendRequest(request.requestId, user.id)) {
                successResponse()
            } else {
                errorResponse("Could not decline the friend request")
            }
        }

        post("/cancel") {
            val request = call.receive<CancelFriendRequest>()
            val user = getLoggedUser()

            if (friendService.cancelFriendRequest(request.requestId, user.id)) {
                successResponse()
            } else {
                errorResponse("Could not cancel the given friend request")
            }
        }

        get("/requests") {
            val user = getLoggedUser()
            dataResponse(friendService.getFriendRequests(user).map {
                FriendRequestResponse(
                    it.first,
                    it.second
                )
            }.toList())
        }

        get("/list") {
            val user = getLoggedUser()
            dataResponse(friendService.getFriendList(user))
        }
    }
}

fun PipelineContext<Unit, ApplicationCall>.getLoggedUser() = User.fromJWTToken(this.call.principal()!!)

suspend fun PipelineContext<Unit, ApplicationCall>.successResponse() = call.respond(SuccessResponse())
suspend fun PipelineContext<Unit, ApplicationCall>.errorResponse(message: String) = call.respond(ErrorResponse(message))
suspend fun PipelineContext<Unit, ApplicationCall>.dataResponse(any: Any) = call.respond(DataResponse(any))
fun Route.jwtAuthenticatedRoute(path: String, build: Route.() -> Unit) =
    authenticate(JWT_AUTH_NAME) { createRouteFromPath(path).apply(build) }
