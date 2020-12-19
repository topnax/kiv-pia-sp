package com.zcu.kiv.pia.tictactoe.database

import org.jetbrains.exposed.dao.id.IntIdTable

object Users: IntIdTable(name = "users") {
    val username = varchar("username", 26)
    val password = text("password")
    val info = text("info")
}