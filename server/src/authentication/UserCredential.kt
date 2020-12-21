package com.zcu.kiv.pia.tictactoe.authentication

import io.ktor.auth.*

class UserCredential(val email: String, val password: String): Credential