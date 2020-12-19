package com.zcu.kiv.pia.tictactoe.authentication

import io.ktor.auth.*

class UserCredential(val username: String, val password: String): Credential