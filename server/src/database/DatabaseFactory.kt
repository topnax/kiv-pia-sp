package com.zcu.kiv.pia.tictactoe.database

import com.zcu.kiv.pia.tictactoe.repository.FriendListRepository
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.mariadb.jdbc.Driver

val logger = KotlinLogging.logger {}

object DatabaseFactory {

    fun init() {

        val address = System.getenv("TTT_DB_ADDRESS")
        val user = System.getenv("TTT_DB_USER")
        val password = System.getenv("TTT_DB_PASSWORD")
        val port = System.getenv("TTT_DB_PORT")
        val db = System.getenv("TTT_DB")

        // TODO remove
        logger.debug { "got credentials $address, $user, $password, $port, $db" }

        Database.connect(
            url = "jdbc:mysql://$address:$port/$db",
            driver = Driver::class.java.name,
            user = user,
            password = password
        )

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Users)
            SchemaUtils.create(FriendRequests)
            SchemaUtils.create(UsersFriendList)
        }
    }

    suspend fun <T> dbQuery(
        block: suspend () -> T
    ): T =
        newSuspendedTransaction { block() }

}