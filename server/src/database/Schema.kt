package com.zcu.kiv.pia.tictactoe.database

import org.jetbrains.exposed.dao.id.IntIdTable

object Users: IntIdTable(name = "users") {
    val email = varchar("email", 50)
    val username = varchar("username", 50)
    val password = text("password")
}