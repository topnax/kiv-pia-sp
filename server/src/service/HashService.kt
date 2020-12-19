package com.zcu.kiv.pia.tictactoe.service

import io.ktor.util.*
import java.util.*

interface HashService {
    fun hashPassword(password: String): String
}

class SHA256Hasher(salt: String = "pia_salt") : HashService {
    private val digestFunction = getDigestFunction("SHA-256") { password ->
        "$salt${password.length}"
    }

    private val encoder = Base64.getEncoder()

    override fun hashPassword(password: String): String = String(encoder.encode(digestFunction(password)))
}