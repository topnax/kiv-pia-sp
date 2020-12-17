package com.zcu.kiv.pia.tictactoe.game

import org.junit.Test

class TicTacToeGameTest {
    @Test
    fun testAddSeed() {
        val g = TicTacToeGame(3, Seed.CROSS, 3)

        // it's not NOUGHT's turn
        assert(!g.addSeed(0, 0, Seed.NOUGHT))

        // OK
        assert(g.addSeed(0, 0, Seed.CROSS))

        // it's not CROSS' turn
        assert(!g.addSeed(0, 0, Seed.CROSS))

        // 0,0 is already taken
        assert(!g.addSeed(0, 0, Seed.NOUGHT))

        // ok
        assert(g.addSeed(0, 1, Seed.NOUGHT))
    }

    @Test
    fun setAddSeed2() {
        val g = TicTacToeGame(3, Seed.CROSS, 3)

        // it's not NOUGHT's turn
        assert(g.addSeed(0, 0, Seed.CROSS))
        assert(g.addSeed(0, 1, Seed.NOUGHT))
        assert(g.addSeed(1, 1, Seed.CROSS))
        assert(g.addSeed(0, 2, Seed.NOUGHT))
        assert(g.addSeed(2, 2, Seed.CROSS))

        assert(g.state == TicTacToeGame.State.WON)
        assert(g.winner!! == Seed.CROSS)
        assert(g.victoriousCells == hashSetOf(g.board[0,0], g.board[1,1], g.board[2,2]))
    }
}
