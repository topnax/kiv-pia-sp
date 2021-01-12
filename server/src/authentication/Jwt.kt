package com.zcu.kiv.pia.tictactoe.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.auth.jwt.*
import java.util.*

data class Token(val token: String)

class JwtConfig(val issuer: String, val secret: String, val validityInMInutes: Int) {


    private val validityInMs = validityInMInutes * 60 * 1000 // 10 hours
    private val algorithm = Algorithm.HMAC512(secret)

    val verifier: JWTVerifier = JWT.require(algorithm).withIssuer(issuer).build()

    fun makeToken(user: UserPrincipal): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("id", user.id)
        .withClaim("email", user.email)
        .withClaim("username", user.username)
        .withClaim("admin", user.admin)
        .withExpiresAt(getExpiration())
        .sign(algorithm)

    private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)

}

