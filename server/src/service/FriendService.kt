package com.zcu.kiv.pia.tictactoe.service

import com.zcu.kiv.pia.tictactoe.database.logger
import com.zcu.kiv.pia.tictactoe.model.FriendRequest
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.model.response.*
import com.zcu.kiv.pia.tictactoe.repository.FriendListRepository
import com.zcu.kiv.pia.tictactoe.repository.FriendRequestRepository

interface FriendService {
    /**
     * Tries to add a new friend request
     */
    suspend fun addFriendRequest(request: FriendRequest): Boolean

    /**
     * Returns friend requests sent to the given user (Pair<ID, requestorUsername>)
     */
    suspend fun getFriendRequests(user: User): List<Pair<Int, String>>

    /**
     * Confirms a friend request
     */
    suspend fun confirmFriendRequest(requestId: Int, userId: Int): Boolean

    /**
     * Cancels a friend request based on the requestId and ID of the requestor
     */
    suspend fun cancelFriendRequest(requestId: Int, requestorId: Int): Boolean

    /**
     * Declines a friend request
     */
    suspend fun declineFriendRequest(requestId: Int, requestedId: Int): Boolean

    /**
     * Cancels a friendship between two users
     */
    suspend fun cancelFriendship(initiatorId: Int, friendId: Int): Boolean

    /**
     * Returns a list of friends of the given user
     */
    suspend fun getFriendList(user: User): List<User>

}

class FriendRequestException(val reason: String) : Exception(reason)

class FriendServiceImpl(
    private val userService: UserService,
    private val friendRequestRepository: FriendRequestRepository,
    private val friendListRepository: FriendListRepository,
    private val realtimeService: RealtimeService,
) :
    FriendService {
    @Throws(FriendRequestException::class)
    override suspend fun addFriendRequest(request: FriendRequest): Boolean {
        if (userService.getUserById(request.requestor) == null || userService.getUserById(request.requested) == null) {
            logger.warn { "Trying to add a friend request in which one of the users does not exist." }
            return false
        }
        if (friendListRepository.isFriendship(request.requestor, request.requested)) {
            logger.warn { "Trying to create a friend request when the users are already in a friendship." }
            return false
        }

        if (friendRequestRepository.getFriendRequest(request) != null) {
            throw FriendRequestException("Friend request already sent")
        }

        if (friendRequestRepository.getFriendRequest(request.inverse()) != null) {
            throw FriendRequestException("This user has already sent you a friend request. Check pending friend requests.")
        }

        val id = friendRequestRepository.addFriendRequest(request)

        userService.getUserById(request.requested)?.let { requestedUser ->
            userService.getUserById(request.requestor)?.let { requestorUser ->
                realtimeService.sendMessage(
                    RealtimeMessage(
                        RealtimeMessage.Namespace.FRIENDREQUESTS,
                        "incomingRequest",
                        NewFriendRequestResponse(
                            FriendRequestResponse(
                                id,
                                requestorUser.username
                            )
                        )
                    ),
                    users = arrayOf(requestedUser)
                )
            }
        }

        return true
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
                userService.getUserById(request.requested)?.let { requested ->
                    userService.getUserById(request.requestor)?.let { requestor ->
                        realtimeService.sendMessage(
                            RealtimeMessage(
                                RealtimeMessage.Namespace.FRIENDS,
                                "newFriend",
                                NewFriendResponse(
                                    requested
                                )
                            ),
                            users = arrayOf(requestor)
                        )
                        realtimeService.sendMessage(
                            RealtimeMessage(
                                RealtimeMessage.Namespace.FRIENDS,
                                "newFriend",
                                NewFriendResponse(
                                    requestor
                                )
                            ),
                            users = arrayOf(requested)
                        )
                    }
                }
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

    override suspend fun cancelFriendship(initiatorId: Int, friendId: Int): Boolean {
        val removed = friendListRepository.removeFriendship(initiatorId, friendId) > 0

        if (removed) {
            logger.info { "Removing a friend" }

            userService.getUserById(initiatorId)?.let { initiator ->
                userService.getUserById(friendId)?.let { friend ->
                    realtimeService.sendMessage(
                        RealtimeMessage(
                            RealtimeMessage.Namespace.FRIENDS,
                            "friendGone",
                            FriendGoneResponse(
                                initiator
                            ),
                        ),
                        users = arrayOf(friend)
                    )
                }
            }
        }
        return removed
    }


    override suspend fun getFriendList(user: User) = friendListRepository.getFriends(user.id)

    private fun FriendRequest.inverse() = FriendRequest(this.requested, this.requestor)
}