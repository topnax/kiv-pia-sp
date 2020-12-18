package com.zcu.kiv.pia.tictactoe.modules

import com.zcu.kiv.pia.tictactoe.service.GameRepository
import com.zcu.kiv.pia.tictactoe.service.GameService
import com.zcu.kiv.pia.tictactoe.service.GameServiceImpl
import org.koin.dsl.module

val mainModule = module {
    single<GameService> { GameServiceImpl(get()) }
    single { GameRepository() }
}