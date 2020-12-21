package com.zcu.kiv.pia.tictactoe.utils

import io.ktor.util.*
import org.junit.Test
import kotlin.test.assertEquals


class PasswordRuleVerifierTest {
    @InternalAPI
    @Test
    fun `verifier should not allow weak passwords`() {
        assertEquals(0, PasswordRuleVerifier.verifyPassword("Password1").size)
        assert(
            PasswordRuleVerifier.verifyPassword("foo").map { it.javaClass }
                .containsAll(
                    listOf(
                        PasswordRuleVerifier.Rule.`Must be longer than 8 characters`().javaClass,
                        PasswordRuleVerifier.Rule.`Must contain atleast one number`().javaClass,
                        PasswordRuleVerifier.Rule.`Must contain uppercase and lowercase`().javaClass
                    )
                )
        )
    }
}