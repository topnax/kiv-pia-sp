package com.zcu.kiv.pia.tictactoe.model

import com.zcu.kiv.pia.tictactoe.database.logger
import io.ktor.auth.jwt.*

data class User(val id: Int, val email: String) {
    companion object {
        fun fromJWTToken(principal: JWTPrincipal): User {
            logger.debug { "about to get emaiul" }
            val email = principal.payload.getClaim("email")?.asString()!!
            logger.debug { "about to get id" }
            val id = principal.payload.getClaim("id")?.asInt()!!
            logger.debug { "got id/email" }
            return User(id, email)
        }
    }
}