package com.zcu.kiv.pia.tictactoe.hasher

import com.zcu.kiv.pia.tictactoe.service.SHA256Hasher
import org.junit.Test

class SHA256HasherTest {
    @Test
    fun `hashing a password`() {
        val password = "PIA_2020"
        val passwordService = SHA256Hasher()
        assert(passwordService.hashPassword(password) == "UMxE5rxJYbHf9Ivn9dYVUsRaqqu3vC4O15XXMYLgsXw=")
    }
}