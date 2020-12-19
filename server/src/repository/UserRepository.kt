package com.zcu.kiv.pia.tictactoe.repository

import com.zcu.kiv.pia.tictactoe.authentication.UserCredential
import com.zcu.kiv.pia.tictactoe.database.DatabaseFactory.dbQuery
import com.zcu.kiv.pia.tictactoe.database.Users

import com.zcu.kiv.pia.tictactoe.model.User
import mu.KotlinLogging
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

private val logger = KotlinLogging.logger {}

interface UserRepository {
    suspend fun getUserByName(username: String): User?

    suspend fun getUsers(): List<User>

    suspend fun userByCredentials(username: String, passwordHash: String): User?
}

class DatabaseUserRepository : UserRepository {

    private fun toUser(row: ResultRow): User =
        User(row[Users.username], row[Users.info])


    override suspend fun getUsers(): List<User> = dbQuery {
        Users.selectAll().map { resultRow ->
            logger.debug { resultRow[Users.username] + " - " + "'${resultRow[Users.password]}'" }
            toUser(resultRow)
        }
    }

    override suspend fun getUserByName(username: String): User? = dbQuery {
        Users.select {
            (Users.username eq username)
        }.mapNotNull { toUser(it) }.singleOrNull()
    }

    override suspend fun userByCredentials(username: String, passwordHash: String): User? = dbQuery {
        Users.select {
            (Users.username eq username) and (Users.password eq passwordHash)
        }.mapNotNull {
            toUser(it)
        }.firstOrNull()
    }
}