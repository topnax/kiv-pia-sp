package com.zcu.kiv.pia.tictactoe.module

import com.zcu.kiv.pia.tictactoe.database.RedisDatabase
import com.zcu.kiv.pia.tictactoe.repository.SQLUserRepository
import com.zcu.kiv.pia.tictactoe.repository.InMemoryUserRepository
import com.zcu.kiv.pia.tictactoe.repository.RedisUserRepository
import com.zcu.kiv.pia.tictactoe.repository.PersistentUserRepository
import com.zcu.kiv.pia.tictactoe.service.*
import org.koin.dsl.module

val mainModule = listOf(
    module {
        single<GameService> { GameServiceImpl(get()) }
        single { GameRepository() }
        single<PersistentUserRepository> { SQLUserRepository() }
        single<HashService> { SHA256Hasher() }

        single { RedisDatabase() }
        single<InMemoryUserRepository> { RedisUserRepository(get()) }
        single<UserService> { UserServiceImpl(get(), get()) }
    }
)