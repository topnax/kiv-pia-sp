package com.zcu.kiv.pia.tictactoe.utils

object PasswordRuleVerifier {

    sealed class Rule(val violationMessage: String, private val validator: (password: String) -> Boolean) {
        class `Must contain uppercase and lowercase` :
            Rule(violationMessage = "The password must contain at least one uppercase and lowercase character.", {
                it.firstOrNull { char -> char.isLetter() && char.isUpperCase() } != null &&
                        it.firstOrNull { char -> char.isLetter() && char.isLowerCase() } != null
            })

        class `Must contain atleast one number` :
            Rule(violationMessage = "The password must contain at least one number.", {
                val ret = it.filter { char ->
                    char.isDigit()
                }.firstOrNull() != null
                println("returning $ret")
                ret
            })

        class `Must not be longer than 20 characters` :
            Rule(violationMessage = "The password must not consist of more than 20 characters.", { it.length <= 20 })

        class `Must be longer than 8 characters` :
            Rule(violationMessage = "The password must consist of at least 8 characters.", { it.length > 7 })

        fun verify(password: String): Boolean = validator(password)
    }

    private val rules = listOf(
        Rule.`Must contain uppercase and lowercase`(),
        Rule.`Must not be longer than 20 characters`(),
        Rule.`Must contain atleast one number`(),
        Rule.`Must be longer than 8 characters`()
    )

    fun verifyPassword(password: String): List<Rule> = rules.filter { !it.verify(password) }
}