package com.zcu.kiv.pia.tictactoe.service

import com.zcu.kiv.pia.tictactoe.database.logger
import com.zcu.kiv.pia.tictactoe.model.FriendRequest
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.repository.FriendListRepository
import com.zcu.kiv.pia.tictactoe.repository.FriendRequestRepository

interface FriendService {
    suspend fun addFriendRequest(request: FriendRequest): Boolean

    suspend fun getFriendRequests(user: User): List<Pair<Int, String>>

    suspend fun confirmFriendRequest(requestId: Int, userId: Int): Boolean

    suspend fun cancelFriendRequest(requestId: Int, requestorId: Int): Boolean

    suspend fun declineFriendRequest(requestId: Int, requestedId: Int): Boolean

    suspend fun cancelFriendship(initiator: Int, friend: Int): Boolean

    suspend fun getFriendList(user: User): List<User>

}

class FriendServiceImpl(
    private val userService: UserService,
    private val friendRequestRepository: FriendRequestRepository,
    private val friendListRepository: FriendListRepository
) :
    FriendService {
    override suspend fun addFriendRequest(request: FriendRequest): Boolean {
        if (userService.getUserById(request.requestor) == null || userService.getUserById(request.requested) == null) {
            logger.warn { "Trying to add a friend request in which one of the users does not exist." }
            return false
        }
        if (friendListRepository.isFriendship(request.requestor, request.requested)) {
            logger.warn { "Trying to create a friend request when the users are already in a friendship." }
            return false
        }
        if (friendRequestRepository.getFriendRequest(request) == null &&
            friendRequestRepository.getFriendRequest(request.inverse()) == null
        ) {
            friendRequestRepository.addFriendRequest(request)
            // TODO notify via websockets
            return true
        }
        logger.warn { "Trying to add a friend request to which an inverse request already exists" }
        return false
    }

    override suspend fun getFriendRequests(user: User) = friendRequestRepository.getFriendRequestsWithUsernames(user)

    override suspend fun confirmFriendRequest(requestId: Int, userId: Int): Boolean {
        val request = friendRequestRepository.getFriendRequestById(requestId)

        friendRequestRepository.getFriendRequestById(requestId)?.let {
            if (request?.requested != userId) {
                logger.warn { "A user tried to accept a request which was not meant to him" }
                return false
            }
            if (!friendListRepository.isFriendship(it.requestor, it.requested)) {
                friendRequestRepository.removeFriendRequest(requestId)
                friendListRepository.addFriendship(it.requestor, it.requested)
                // TODO notify via websockets
                return true
            } else {
                logger.warn { "Trying to confirm a friend request when the users are already friends." }
            }
        } ?: run {
            logger.warn { "Tried to confirm non-existent friend request" }
        }
        return false
    }

    override suspend fun cancelFriendRequest(requestId: Int, requestorId: Int): Boolean {
        val request = friendRequestRepository.getFriendRequestById(requestId)

        request?.let {
            if (it.requestor == requestorId) {
                // TODO notify via websockets
                return friendRequestRepository.removeFriendRequest(requestId)
            } else {
                logger.warn { "Tried to cancel a request which did not belong to the given requestor" }
            }
        } ?: run {
            logger.warn { "Could not cancel a friend request because the given friend request was not found" }
        }
        return false
    }

    override suspend fun declineFriendRequest(requestId: Int, requestedId: Int): Boolean {
        val request = friendRequestRepository.getFriendRequestById(requestId)

        request?.let {
            if (it.requested == requestedId) {
                // TODO notify via websockets
                return friendRequestRepository.removeFriendRequest(requestId)
            } else {
                logger.warn { "Tried to decline a request which did not belong to the given requested user" }
            }
        } ?: run {
            logger.warn { "Could not decline a friend request because the given friend request was not found" }
        }
        return false
    }

    override suspend fun cancelFriendship(initiator: Int, friend: Int): Boolean {
        return friendListRepository.removeFriendship(initiator, friend) > 0
    }

    override suspend fun getFriendList(user: User) = friendListRepository.getFriends(user.id)

    private fun FriendRequest.inverse() = FriendRequest(this.requested, this.requestor)
}