package com.zcu.kiv.pia.tictactoe.repository

import com.zcu.kiv.pia.tictactoe.database.DatabaseFactory.dbQuery
import com.zcu.kiv.pia.tictactoe.database.GameResults
import com.zcu.kiv.pia.tictactoe.database.GameTurns
import com.zcu.kiv.pia.tictactoe.database.Users
import com.zcu.kiv.pia.tictactoe.game.Cell
import com.zcu.kiv.pia.tictactoe.game.TicTacToeGame
import com.zcu.kiv.pia.tictactoe.model.GameResult
import com.zcu.kiv.pia.tictactoe.model.GameTurn
import com.zcu.kiv.pia.tictactoe.model.User
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*

interface GameResultRepository {
    suspend fun addResult(crossWon: Boolean, crossUserId: Int, noughtUserId: Int, turns: List<TicTacToeGame.Turn>, boardSize: Int, victoriousTurns: List<TicTacToeGame.Turn>)

    suspend fun getResultsByUserId(userId: Int): List<GameResult>

    suspend fun getResultById(id: Int): GameResult?

    suspend fun getTurnsByGameResultId(gameResultId: Int): List<GameTurn>
}

class SQLGameResultRepository : GameResultRepository {

    private fun toGameResult(row: ResultRow): GameResult =
        GameResult(
            row[GameResults.id].value,
            row[GameResults.crossWon],
            row[GameResults.crossUserId],
            row[GameResults.noughtUserId],
            row[GameResults.dateCreated],
            row[GameResults.boardSize]
        )

    private fun toGameTurn(row: ResultRow): GameTurn =
        GameTurn(
            row[GameTurns.id].value,
            row[GameTurns.row],
            row[GameTurns.column],
            row[GameTurns.seed],
            row[GameTurns.victorious]
        )

    override suspend fun addResult(
        crossWon: Boolean,
        crossUserId: Int,
        noughtUserId: Int,
        turns: List<TicTacToeGame.Turn>,
        boardSize: Int,
        victoriousTurns: List<TicTacToeGame.Turn>
    ): Unit = dbQuery {

        val id = GameResults.insert {
            it[GameResults.crossWon] = crossWon
            it[GameResults.crossUserId] = crossUserId
            it[GameResults.noughtUserId] = noughtUserId
            it[GameResults.boardSize] = boardSize
        }[GameResults.id]

        turns.forEach { turn ->
            GameTurns.insert {
                it[GameTurns.row] = turn.row
                it[GameTurns.column] = turn.column
                it[GameTurns.seed] = turn.seed.toString()[0]
                it[GameTurns.gameId] = id.value
                it[GameTurns.victorious] = victoriousTurns.contains(turn)
            }
        }

    }

    override suspend fun getResultById(id: Int) = dbQuery {
        GameResults.select {
            (GameResults.id eq id)
        }.mapNotNull { toGameResult(it) }.singleOrNull()
    }

    override suspend fun getResultsByUserId(userId: Int) = dbQuery {
        GameResults.select {
            (GameResults.crossUserId eq userId) or (GameResults.noughtUserId eq userId)
        }.orderBy(GameResults.dateCreated to SortOrder.DESC).mapNotNull { toGameResult(it) }
    }

    override suspend fun getTurnsByGameResultId(gameResultId: Int) = dbQuery {
        GameTurns.select {
            (GameTurns.gameId eq gameResultId)
        }.mapNotNull { toGameTurn(it) }.toList()
    }

}