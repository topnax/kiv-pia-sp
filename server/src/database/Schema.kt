package com.zcu.kiv.pia.tictactoe.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.CurrentDateTime
import org.jetbrains.exposed.sql.`java-time`.datetime

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

object GameResults: IntIdTable(name = "game_results") {
    val draw = bool("draw").default(false)
    val crossWon = bool("cross_won")
    val crossUserId = integer("cross_user")
        .references(Users.id)
    val noughtUserId = integer("nought_user")
        .references(Users.id)

    val boardSize = integer("board_size")

    val dateCreated = datetime("date_played").defaultExpression(CurrentDateTime())
}

object GameTurns: IntIdTable(name = "game_turns") {
   val gameId = integer("game_id")
       .references(GameResults.id)

    val row = integer("row")
    val column = integer("column")
    val seed = char("seed")
    val victorious = bool("victorious")
}