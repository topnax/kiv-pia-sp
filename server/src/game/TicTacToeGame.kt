package com.zcu.kiv.pia.tictactoe.game

class TicTacToeGame(val boardSize: Int, startingSeed: Seed, private val victoriousCellCount: Int) {

    val turns = mutableListOf<Turn>()

    /**
     * Game board
     */
    val board: Board = Board(boardSize, boardSize)

    /**
     * A flag indicating whose turn it is
     */
    var currentSeed = startingSeed

    /**
     * A flag indicating whether the game has finished
     */
    var state = State.IN_PROGRESS

    /**
     * An attribute used for storing the winner of the game
     */
    var winner: Seed? = null

    val victoriousCells = hashSetOf<Cell>()

    fun addSeed(row: Int, column: Int, seed: Seed): Boolean {
        // game has finished, can't add any more seeds
        if (state != State.IN_PROGRESS) return false

        // it is not turn of the given seed
        if (currentSeed != seed) return false

        // could not place the cell onto the board
        if (!board.addCell(Cell(row, column).apply { value = seed })) return false

        // add the turn to the turn history
        turns.add(Turn(seed, row, column))

        // instantiate a board checker
        val checker = SimpleBoardChecker(board, victoriousCellCount)

        // check whether the turn was victorious
        if (checker.isCellVictorious(row, column)) {
            state = State.WON
            // save the victorious cells if the game has finished
            victoriousCells += checker.getVictoriousCells()
            winner = currentSeed
        } else if (!board.isPlayable()) {
            state = State.DRAW
        }

        // set the next seed
        currentSeed = if (currentSeed == Seed.CROSS) Seed.NOUGHT else Seed.CROSS

        return true
    }

    class Turn(val seed: Seed, val row: Int, val column: Int)

    enum class State {
        IN_PROGRESS,
        WON,
        DRAW
    }

}