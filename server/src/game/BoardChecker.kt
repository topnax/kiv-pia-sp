package com.zcu.kiv.pia.tictactoe.game

abstract class BoardChecker(val board: Board, val victoriousCells: Int) {
    abstract fun isCellVictorious(row: Int, column: Int): Boolean
    abstract fun getVictoriousCells(): Set<Cell>
}

class SimpleBoardChecker(board: Board, victoriousCells: Int) : BoardChecker(board, victoriousCells) {

    private val directions = listOf(
        // diagonal, from top left to bottom right
        Pair(Pair(-1, -1), Pair(1, 1)),

        // horizontal
        Pair(Pair(0, -1), Pair(0, 1)),

        // diagonal, from bottom left to top right
        Pair(Pair(1, -1), Pair(-1, 1)),

        // vertical
        Pair(Pair(-1, 0), Pair(1, 0)),
    )

    /**
     * Field holding last victorious cells
     */
    private val winningCells = HashSet<Cell>()

    /**
     * Returns `true` when the cell is within a group of victorious cells
     */
    override fun isCellVictorious(row: Int, column: Int): Boolean {
        val cell = board.getCell(row, column)

        cell?.let {
            // iterate over possible winning directions
            for (direction in directions) {
                // get all cells with the same cell
                val cellSet1 = getCellsWithSameSeed(cell, direction.first.first, direction.first.second)
                val cellSet2 = getCellsWithSameSeed(cell, direction.second.first, direction.second.second)
                if (cellSet1.size + cellSet2.size + 1 >= victoriousCells) {
                    winningCells.clear()
                    winningCells.add(it)
                    winningCells += cellSet1 + cellSet2
                    return true
                }
            }
        }
        return false
    }

    override fun getVictoriousCells(): Set<Cell> = winningCells

    fun getCellsWithSameSeed(cell: Cell, dr: Int, dc: Int): Set<Cell> {
        val cells = HashSet<Cell>()
        val seed = cell.value
        var r = cell.row
        var c = cell.column
        while (true) {
            // move by the direction
            r += dr * 1
            c += dc * 1
            // check whether a cell exists at the new coordinates
            board.getCell(r, c)?.let {
                // check whether the seed matches
                if (it.value == seed) {
                    cells.add(it)
                } else {
                    return cells
                }
            } ?: run {
                return cells
            }
        }
    }
}