package com.zcu.kiv.pia.tictactoe.game

interface BoardChecker {
    fun isTurnVictorious(board: Board, row: Int, column: Int, victoriousCells: Int): Boolean
}

class SimpleBoardChecker: BoardChecker {
    override fun isTurnVictorious(board: Board, row: Int, column: Int, victoriousCells: Int): Boolean {
        return false
    }
}