package com.zcu.kiv.pia.tictactoe.service.game

import com.zcu.kiv.pia.tictactoe.game.Seed
import com.zcu.kiv.pia.tictactoe.model.GameResult
import com.zcu.kiv.pia.tictactoe.model.GameTurn
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.repository.GameResultRepository
import com.zcu.kiv.pia.tictactoe.service.GameWrapper
import com.zcu.kiv.pia.tictactoe.service.UserService

interface GameResultsService {
    suspend fun addGameResult(gameWrapper: GameWrapper)

    suspend fun getGameResultsByUser(user: User): List<GameResult>

    suspend fun getGameResultById(id: Int): GameResult?

    suspend fun getTurnsByGameResultId(gameResultId: Int): List<GameTurn>
}

class GameResultsServiceImpl(
    private val gameResultRepository: GameResultRepository,
    private val userService: UserService
) : GameResultsService {
    override suspend fun addGameResult(gameWrapper: GameWrapper) {
        gameResultRepository.addResult(
            gameWrapper.game.winner == Seed.CROSS,
            gameWrapper.cross.id,
            gameWrapper.nought.id,
            gameWrapper.game.turns,
            gameWrapper.game.boardSize
        )
    }

    override suspend fun getGameResultById(id: Int) = gameResultRepository.getResultById(id)?.apply {
        crossUsername = userService.getUserById(crossUserId)?.username
        noughtUsername = userService.getUserById(noughtUserId)?.username
    }

    override suspend fun getGameResultsByUser(user: User) = gameResultRepository.getResultsByUserId(user.id).onEach {
        it.crossUsername = userService.getUserById(it.crossUserId)?.username
        it.noughtUsername = userService.getUserById(it.noughtUserId)?.username
    }

    override suspend fun getTurnsByGameResultId(gameResultId: Int) =
        gameResultRepository.getTurnsByGameResultId(gameResultId)

}