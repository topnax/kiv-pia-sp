package com.zcu.kiv.pia.tictactoe.repository

import com.zcu.kiv.pia.tictactoe.database.DatabaseFactory.dbQuery
import com.zcu.kiv.pia.tictactoe.database.Users
import com.zcu.kiv.pia.tictactoe.database.UsersFriendList
import com.zcu.kiv.pia.tictactoe.model.User
import io.ktor.http.*
import org.jetbrains.exposed.sql.*

interface FriendListRepository {
    suspend fun addFriendship(userId1: Int, userId2: Int)

    suspend fun removeFriendship(userId1: Int, userId2: Int): Int

    suspend fun getFriends(id: Int): List<User>

    suspend fun isFriendship(userId1: Int, userId2: Int): Boolean
}

class SQLFriendListRepository : FriendListRepository {

    override suspend fun addFriendship(userId1: Int, userId2: Int): Unit = dbQuery {
        UsersFriendList.insert {
            it[UsersFriendList.user1Id] = userId1
            it[UsersFriendList.user2Id] = userId2
        }
    }

    override suspend fun removeFriendship(userId1: Int, userId2: Int): Int = dbQuery {
        return@dbQuery UsersFriendList.deleteWhere {
            (((UsersFriendList.user1Id eq userId1) and (UsersFriendList.user2Id eq userId2))
                    or
                    ((UsersFriendList.user1Id eq userId2) and (UsersFriendList.user2Id eq userId1)))
        }
    }

    override suspend fun getFriends(id: Int): List<User> = dbQuery {
        Users.join(UsersFriendList, JoinType.INNER, additionalConstraint = {(Users.id eq UsersFriendList.user1Id) or (Users.id eq UsersFriendList.user2Id)})
            .select {
            ((UsersFriendList.user2Id eq id) or (UsersFriendList.user2Id eq id)) and (Users.id neq id)
        }.map {
            User(it[Users.id].value, it[Users.email], it[Users.username])
        }.toList()
    }

    override suspend fun isFriendship(userId1: Int, userId2: Int): Boolean = dbQuery {
        !(UsersFriendList.select {
            (((UsersFriendList.user1Id eq userId1) and (UsersFriendList.user2Id eq userId2)) or ((UsersFriendList.user1Id eq userId2) and (UsersFriendList.user2Id eq userId1)))
        }.empty())
    }

}