package com.zcu.kiv.pia.tictactoe.model

import com.zcu.kiv.pia.tictactoe.database.logger
import io.ktor.auth.jwt.*

data class User(val id: Int, val email: String, val username: String, val admin: Boolean = false) {
    companion object {
        fun fromJWTToken(principal: JWTPrincipal): User {
            val email = principal.payload.getClaim("email")?.asString()!!
            val username = principal.payload.getClaim("username")?.asString()!!
            val id = principal.payload.getClaim("id")?.asInt()!!
            val admin = principal.payload.getClaim("admin")?.asBoolean()!!
            return User(id, email, username, admin)
        }
    }
}