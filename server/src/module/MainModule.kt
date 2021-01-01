package com.zcu.kiv.pia.tictactoe.module

import com.zcu.kiv.pia.tictactoe.database.RedisDatabase
import com.zcu.kiv.pia.tictactoe.repository.*
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
        single<RealtimeService> { WebsocketService(get()) }
        single<UserService> { UserServiceImpl(get(), get(), get()) }

        single<FriendListRepository> { SQLFriendListRepository() }
        single<FriendRequestRepository> { SQLFriendRequestRepository() }
        single<FriendService> { FriendServiceImpl(get(), get(), get()) }
    }
)