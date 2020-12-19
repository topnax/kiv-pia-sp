package com.zcu.kiv.pia.tictactoe.modules

import com.zcu.kiv.pia.tictactoe.repository.DatabaseUserRepository
import com.zcu.kiv.pia.tictactoe.repository.UserRepository
import com.zcu.kiv.pia.tictactoe.service.*
import org.koin.dsl.module

val mainModules = listOf(
    module {
        single<GameService> { GameServiceImpl(get()) }
        single { GameRepository() }
    },
    module {
        single<UserRepository> { DatabaseUserRepository() }
    },
    module {
        single<HashService> { SHA256Hasher() }
    }
)