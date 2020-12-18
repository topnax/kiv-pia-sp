package com.zcu.kiv.pia.tictactoe.service

interface GameService {
    fun playGame(): String
}

class GameRepository {
    fun getGame() = "Tic Tac Toe game played :)"
}

class GameServiceImpl(private val gameRepository: GameRepository): GameService {
    override fun playGame() = "Game status: ${gameRepository.getGame()}"
}