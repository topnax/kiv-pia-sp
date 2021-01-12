package com.zcu.kiv.pia.tictactoe.database

import com.zcu.kiv.pia.tictactoe.service.HashService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.mariadb.jdbc.Driver

val logger = KotlinLogging.logger {}

object DatabaseFactory {

    fun init(hashService: HashService) {

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
            SchemaUtils.create(GameResults)
            SchemaUtils.create(GameTurns)
            SchemaUtils.createMissingTablesAndColumns(GameResults)
            SchemaUtils.createMissingTablesAndColumns(Users)
            if (Users.select { Users.username eq "admin" }.singleOrNull() == null) {
                logger.info { "Creating admin user" }
                Users.insert {
                    it[Users.username] = "admin"
                    it[Users.password] = hashService.hashPassword("admin")
                    it[Users.admin] = true
                    it[Users.email] = "admin@ttt-game.com"
                }
            }
        }
    }

    suspend fun <T> dbQuery(
        block: suspend () -> T
    ): T =
        newSuspendedTransaction { block() }

}