package com.zcu.kiv.pia.tictactoe.service

import io.ktor.application.*

interface ConfigurationService {
    val jwtIssuer: String
    val jwtSecret: String
    val jwtAudience: String
    val jwtRealm: String

}

class ConfigurationServiceImpl(environment: ApplicationEnvironment) : ConfigurationService {
    override val jwtIssuer = environment.config.property("jwt.domain").getString()
    override val jwtSecret = environment.config.property("jwt.secret").getString()
    override val jwtAudience = environment.config.property("jwt.audience").getString()
    override val jwtRealm = environment.config.property("jwt.realm").getString()
}
