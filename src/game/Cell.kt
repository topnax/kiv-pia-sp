package com.zcu.kiv.pia.tictactoe.game

class Cell(val row: Int, val column: Int, var value: Seed = Seed.EMPTY) {
    fun clear() {
        value = Seed.EMPTY
    }
}

enum class Seed {
    CROSS, NOUGHT, EMPTY
}