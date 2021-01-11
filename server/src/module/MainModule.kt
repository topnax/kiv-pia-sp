package com.zcu.kiv.pia.tictactoe.module

import com.zcu.kiv.pia.tictactoe.database.RedisDatabase
import com.zcu.kiv.pia.tictactoe.repository.*
import com.zcu.kiv.pia.tictactoe.service.*
import com.zcu.kiv.pia.tictactoe.service.game.GameMessagingService
import com.zcu.kiv.pia.tictactoe.service.game.GameMessagingServiceImpl
import com.zcu.kiv.pia.tictactoe.service.game.GameResultsService
import com.zcu.kiv.pia.tictactoe.service.game.GameResultsServiceImpl
import com.zcu.kiv.pia.tictactoe.service.lobby.LobbyMessagingService
import com.zcu.kiv.pia.tictactoe.service.lobby.LobbyMessagingServiceImpl
import org.koin.dsl.module

val mainModule = listOf(
    module {

        single<GameResultRepository> { SQLGameResultRepository() }
        single<GameResultsService> { GameResultsServiceImpl(get()) }

        single<GameMessagingService> { GameMessagingServiceImpl(get()) }
        single<GameService> { GameServiceImpl(get(), get(), get(), get(), get()) }
        single { GameRepository() }
        single<PersistentUserRepository> { SQLUserRepository() }
        single<HashService> { SHA256Hasher() }

        single { RedisDatabase() }
        single<InMemoryUserRepository> { RedisUserRepository(get()) }
        single<RealtimeService> { WebsocketService(get()) }
        single<UserService> { UserServiceImpl(get(), get(), get()) }

        single<NotificationService> { NotificationServiceImpl(get()) }

        single<LobbyMessagingService> { LobbyMessagingServiceImpl(get()) }
        single<LobbyService> { LobbyServiceImpl(get(), get()) }

        single<FriendListRepository> { SQLFriendListRepository() }
        single<FriendRequestRepository> { SQLFriendRequestRepository() }
        single<FriendService> { FriendServiceImpl(get(), get(), get(), get()) }
    }
)