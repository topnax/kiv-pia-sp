package com.zcu.kiv.pia.tictactoe.model.response

import com.zcu.kiv.pia.tictactoe.model.FriendRequest
import com.zcu.kiv.pia.tictactoe.model.User

class UserOnlineResponse(user: User) {
    val username = user.username
    val id = user.id
}

class UserOfflineResponse(user: User) {
    val username = user.username
    val id = user.id
}

class UsersOnlineResponse(users: List<User>) {
    val users: List<Any> = users.map {
        object {
            val username = it.username
            val id = it.id
        }
    }
}

class NotificationResponse(val text: String)

class NewFriendRequestResponse(val request: FriendRequestResponse)

class NewFriendResponse(user: User) {
    val username = user.username
    val id = user.id
}