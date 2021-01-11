package com.zcu.kiv.pia.tictactoe.service.game

import com.zcu.kiv.pia.tictactoe.game.Seed
import com.zcu.kiv.pia.tictactoe.model.GameResult
import com.zcu.kiv.pia.tictactoe.model.GameTurn
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.repository.GameResultRepository
import com.zcu.kiv.pia.tictactoe.service.GameWrapper

interface GameResultsService {
    suspend fun addGameResult(gameWrapper: GameWrapper)

    suspend fun getGameResultsByUser(user: User): List<GameResult>

    suspend fun getGameResultById(id: Int): GameResult?

    suspend fun getTurnsByGameResultId(gameResultId: Int): List<GameTurn>
}

class GameResultsServiceImpl(private val gameResultRepository: GameResultRepository): GameResultsService {
    override suspend fun addGameResult(gameWrapper: GameWrapper) {
        gameResultRepository.addResult(
            gameWrapper.game.winner == Seed.CROSS,
            gameWrapper.cross.id,
            gameWrapper.nought.id,
            gameWrapper.game.turns,
            gameWrapper.game.boardSize
        )
    }

    override suspend fun getGameResultById(id: Int) = gameResultRepository.getResultById(id)

    override suspend fun getGameResultsByUser(user: User) = gameResultRepository.getResultsByUserId(user.id)

    override suspend fun getTurnsByGameResultId(gameResultId: Int) = gameResultRepository.getTurnsByGameResultId(gameResultId)

}