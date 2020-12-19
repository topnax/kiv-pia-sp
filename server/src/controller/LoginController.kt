package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.authentication.JwtConfig
import com.zcu.kiv.pia.tictactoe.authentication.UserCredential
import com.zcu.kiv.pia.tictactoe.authentication.UserPrincipal
import com.zcu.kiv.pia.tictactoe.repository.UserRepository
import com.zcu.kiv.pia.tictactoe.service.GameRepository
import com.zcu.kiv.pia.tictactoe.service.HashService
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging
import org.koin.ktor.ext.inject

private val logger = KotlinLogging.logger {}

fun Route.loginRoutes(jvtConfig: JwtConfig) {
    val gameRepository: GameRepository by inject()
    val userRepository: UserRepository by inject()
    val hashService: HashService by inject()
    post("/login") {
        logger.debug { gameRepository.getGame() }
        val credentials = call.receive<UserCredential>()
        userRepository.getUsers().forEach{
            logger.debug { "username=${it.name}, info=${it.info}" }
        }
        val user = userRepository.userByCredentials(
            credentials.username,
            hashService.hashPassword(credentials.password)
        )
        if (user == null) {
            call.respond(HttpStatusCode.Unauthorized)
        } else {
            call.respond(jvtConfig.makeToken(UserPrincipal(user.name, user.info)))
        }
    }
}