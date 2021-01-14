package com.zcu.kiv.pia.tictactoe.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

data class Token(val token: String)

class JwtConfig(private val issuer: String, secret: String, validityInMinutes: Int) {

    private val validityInMs = validityInMinutes * 60 * 1000
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

