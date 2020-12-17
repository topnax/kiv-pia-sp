package com.zcu.kiv.pia.tictactoe.game

class Board(val rows: Int, val columns: Int) {
    private val cells = Array(rows) { Array<Cell?>(columns) { null } }

    fun addCell(c: Cell): Boolean {
        return if (!canCellBePlaced(c)) {
            // cell already used or invalid cell position specified
            false
        } else {
            // cell not use - assign it
            cells[c.row][c.column] = c
            true
        }
    }

    fun getCell(row: Int, column: Int) = if (isPositionValid(row, column)) cells[row][column] else null

    operator fun get(row:Int, column: Int) = getCell(row, column)

    fun canCellBePlaced(c: Cell) = isPositionValid(c.row, c.column) && cells[c.row][c.column] == null

    fun isPositionValid(row: Int, column: Int) = !(row < 0 || column < 0 || row >= rows || column >= columns)

}

