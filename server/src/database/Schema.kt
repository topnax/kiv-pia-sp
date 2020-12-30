package com.zcu.kiv.pia.tictactoe.database

import org.jetbrains.exposed.dao.id.IntIdTable

object Users: IntIdTable(name = "users") {
    val email = varchar("email", 50)
    val username = varchar("username", 50)
    val password = text("password")
}

object UsersFriendList: IntIdTable(name = "users_friend_list") {
    val user1Id = integer("user_1_id")
        .references(Users.id)

    val user2Id = integer("user_2_id")
        .references(Users.id)
}

object FriendRequests: IntIdTable(name = "friend_requests") {
    val requestor = integer("requestor")
        .references(Users.id)

    val requested = integer("requested")
        .references(Users.id)
}