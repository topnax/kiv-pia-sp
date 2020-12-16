package com.zcu.kiv.pia.tictactoe.game

class Board(val rows: Int, val columns: Int) {
    val cells = Array(rows) { Array<Cell?>(columns) { null } }

    fun addCell(c: Cell): Boolean {

        if (c.row < 0 || c.column < 0 || c.row >= rows || c.column >= columns || cells[c.row][c.column] != null) {
            // cell already used or invalid cell position specified
            return false
        } else {
            // cell not use - assign it
            cells[c.row][c.column] = c
            return true
        }
    }

}

