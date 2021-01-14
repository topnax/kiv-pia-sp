package com.zcu.kiv.pia.tictactoe.database

import com.zcu.kiv.pia.tictactoe.service.HashService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.mariadb.jdbc.Driver

private const val DATABASE_CONNECT_MAX_ATTEMPT_COUNT = 5
private const val DATABASE_CONNECT_ATTEMPT_TIMEOUT = 5000L

val logger = KotlinLogging.logger {}

object DatabaseFactory {

    fun init(hashService: HashService) {

        val address = System.getenv("TTT_DB_ADDRESS")
        val user = System.getenv("TTT_DB_USER")
        val password = System.getenv("TTT_DB_PASSWORD")
        val port = System.getenv("TTT_DB_PORT")
        val db = System.getenv("TTT_DB")

        logger.debug { "got db credentials $address, $user, $port, $db" }

        Database.connect(
            url = "jdbc:mysql://$address:$port/$db",
            driver = Driver::class.java.name,
            user = user,
            password = password
        )

        var initialized = false
        var attempt = 0
        while (!initialized && attempt < DATABASE_CONNECT_MAX_ATTEMPT_COUNT) {
            logger.info { "Trying to initialize db" }
            try {
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
                initialized = true
            } catch (ex: Exception) {;
                logger.error { "Database initialization attempt #${attempt + 1} has failed..." }
                logger.error { ex }
                attempt++
                Thread.sleep(DATABASE_CONNECT_ATTEMPT_TIMEOUT)
            }
            logger.info { "DB initialized" }
        }
        if (!initialized) {
            throw Exception("Could not initialize the database")
        }
    }

    suspend fun <T> dbQuery(
        block: suspend () -> T
    ): T =
        newSuspendedTransaction { block() }

}