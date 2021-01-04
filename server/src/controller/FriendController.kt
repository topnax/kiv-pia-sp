package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.model.FriendRequest
import com.zcu.kiv.pia.tictactoe.model.response.FriendRequestResponse
import com.zcu.kiv.pia.tictactoe.request.*
import com.zcu.kiv.pia.tictactoe.service.FriendRequestException
import com.zcu.kiv.pia.tictactoe.service.FriendService
import com.zcu.kiv.pia.tictactoe.utils.*
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject

fun Route.friendRoutes() {
    val friendService: FriendService by inject()
    jwtAuthenticatedRoute("/friend") {
        post("/new") {
            val request = call.receive<NewFriendRequest>()
            val user = getLoggedUser()

            try {
                if (friendService.addFriendRequest(FriendRequest(user.id, request.userId))) {
                    successResponse()
                } else {
                    errorResponse("Could not create a new friend request")
                }
            } catch (ex: FriendRequestException) {
                errorResponse(ex.reason)
            }
        }

        post("/acceptRequest") {
            val request = call.receive<AcceptFriendRequest>()
            val user = getLoggedUser()

            if (friendService.confirmFriendRequest(request.requestId, user.id)) {
                successResponse()
            } else {
                errorResponse("Could not confirm the friend request")
            }
        }

        post("/declineRequest") {
            val request = call.receive<DeclineFriendRequest>()
            val user = getLoggedUser()

            if (friendService.declineFriendRequest(request.requestId, user.id)) {
                successResponse()
            } else {
                errorResponse("Could not decline the friend request")
            }
        }

        post("/cancelRequest") {
            val request = call.receive<CancelFriendRequestRequest>()
            val user = getLoggedUser()

            if (friendService.cancelFriendRequest(request.requestId, user.id)) {
                successResponse()
            } else {
                errorResponse("Could not cancel the given friend request")
            }
        }

        post("/cancel") {
            val request = call.receive<CancelFriendshipRequest>()
            val user = getLoggedUser()

            if (friendService.cancelFriendship(user.id, request.userId)) {
                successResponse()
            } else {
                errorResponse("Could not cancel a friendship.")
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


