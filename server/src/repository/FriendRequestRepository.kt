package com.zcu.kiv.pia.tictactoe.repository

import com.zcu.kiv.pia.tictactoe.database.DatabaseFactory.dbQuery
import com.zcu.kiv.pia.tictactoe.database.FriendRequests
import com.zcu.kiv.pia.tictactoe.database.Users
import com.zcu.kiv.pia.tictactoe.model.FriendRequest
import com.zcu.kiv.pia.tictactoe.model.User
import org.jetbrains.exposed.sql.*

interface FriendRequestRepository {
    suspend fun getFriendRequests(user: User): List<FriendRequest>

    suspend fun getFriendRequest(friendRequest: FriendRequest): FriendRequest?

    suspend fun getFriendRequestsWithUsernames(user: User): List<Pair<Int, String>>

    suspend fun getFriendRequestById(id: Int): FriendRequest?

    suspend fun addFriendRequest(friendRequest: FriendRequest): Int

    suspend fun removeFriendRequest(requestId: Int): Boolean
}

class SQLFriendRequestRepository : FriendRequestRepository {
    private fun toFriendRequest(row: ResultRow) =
        FriendRequest(row[FriendRequests.requestor], row[FriendRequests.requested], row[FriendRequests.id].value)

    override suspend fun getFriendRequests(user: User) = dbQuery {
        Users.join(FriendRequests, JoinType.INNER, additionalConstraint = { Users.id eq FriendRequests.requested })
            .slice(Users.username, FriendRequests.id)
        FriendRequests.select {
            (FriendRequests.requested eq user.id)
        }.map {
            toFriendRequest(it)
        }.toList()
    }

    override suspend fun getFriendRequestsWithUsernames(user: User): List<Pair<Int, String>> = dbQuery {
        Users.join(FriendRequests, JoinType.INNER, additionalConstraint = { Users.id eq FriendRequests.requestor })
            .slice(Users.username, FriendRequests.id)
            .select {
                (FriendRequests.requested eq user.id)
            }
            .map { Pair(it[FriendRequests.id].value, it[Users.username]) }
            .toList()
    }

    override suspend fun getFriendRequestById(id: Int): FriendRequest? = dbQuery {
        FriendRequests.select {
            (FriendRequests.id eq id)
        }.mapNotNull { toFriendRequest(it) }.singleOrNull()
    }

    override suspend fun getFriendRequest(friendRequest: FriendRequest): FriendRequest? = dbQuery {
        FriendRequests.select {
            (FriendRequests.requested eq friendRequest.requested) and (FriendRequests.requestor eq friendRequest.requestor)
        }.mapNotNull { friendRequest }.singleOrNull()
    }

    override suspend fun addFriendRequest(friendRequest: FriendRequest): Int = dbQuery {
        FriendRequests.insert {
            it[FriendRequests.requestor] = friendRequest.requestor
            it[FriendRequests.requested] = friendRequest.requested
        } [FriendRequests.id].value
    }

    override suspend fun removeFriendRequest(requestId: Int): Boolean = dbQuery {
        val deleted = FriendRequests.deleteWhere {
            (FriendRequests.id eq requestId)
        }
        return@dbQuery deleted > 0
    }
}