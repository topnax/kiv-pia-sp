package com.zcu.kiv.pia.tictactoe.repository

import com.zcu.kiv.pia.tictactoe.database.DatabaseFactory.dbQuery
import com.zcu.kiv.pia.tictactoe.database.Users
import com.zcu.kiv.pia.tictactoe.model.User
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*

private val logger = KotlinLogging.logger {}

interface PersistentUserRepository {
    suspend fun addUser(email: String, username: String, password: String)

    suspend fun getUserById(id: Int): User?

    suspend fun getUserByEmail(email: String): User?

    suspend fun getUserByUsername(username: String): User?

    suspend fun getUsers(): List<User>

    suspend fun userByCredentials(email: String, passwordHash: String): User?

    suspend fun updateUserPassword(user: User, passwordHash: String)

    suspend fun updateUserAdminStatus(user: User, admin: Boolean)
}

class SQLUserRepository : PersistentUserRepository {

    private fun toUser(row: ResultRow): User =
        User(row[Users.id].value, row[Users.email], row[Users.username], row[Users.admin])

    override suspend fun addUser(email: String, username: String, password: String): Unit = dbQuery {
        Users.insert {
            it[Users.email] = email
            it[Users.username] = username
            it[Users.password] = password
        }
    }

    override suspend fun getUsers(): List<User> = dbQuery {
        Users.selectAll().map { resultRow ->
            toUser(resultRow)
        }
    }

    override suspend fun getUserByEmail(email: String): User? = dbQuery {
        Users.select {
            (Users.email eq email)
        }.mapNotNull { toUser(it) }.singleOrNull()
    }

    override suspend fun getUserById(id: Int) = dbQuery {
        Users.select {
            (Users.id eq id)
        }.mapNotNull { toUser(it) }.singleOrNull()
    }

    override suspend fun getUserByUsername(username: String): User? = dbQuery {
        Users.select {
            (Users.username eq username)
        }.mapNotNull { toUser(it) }.singleOrNull()
    }

    override suspend fun userByCredentials(email: String, passwordHash: String): User? = dbQuery {
        Users.select {
            (Users.email eq email) and (Users.password eq passwordHash)
        }.mapNotNull {
            toUser(it)
        }.firstOrNull()
    }

    override suspend fun updateUserPassword(user: User, passwordHash: String): Unit = dbQuery {
        Users.update({ Users.email eq user.email }) {
            it[password] = passwordHash
        }
    }

    override suspend fun updateUserAdminStatus(user: User, admin: Boolean): Unit = dbQuery {
       Users.update ({Users.id eq user.id} ) {
           it[Users.admin] = admin
       }
    }
}