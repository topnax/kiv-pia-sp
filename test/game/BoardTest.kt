package com.zcu.kiv.pia.tictactoe.game

import org.junit.Test

class BoardTest {
    @Test
    fun testRoot() {
        val b = Board(3, 3);
        assert(b.addCell(Cell(0,0)))
        assert(b.addCell(Cell(1,0)))
        assert(b.addCell(Cell(2,0)))
        assert(b.addCell(Cell(0,1)))
        assert(b.addCell(Cell(1,1)))
        assert(b.addCell(Cell(2,1)))
        assert(b.addCell(Cell(0,2)))
        assert(b.addCell(Cell(1,2)))
        assert(b.addCell(Cell(2,2)))

        assert(!b.addCell(Cell(0,0)))
        assert(!b.addCell(Cell(1,0)))
        assert(!b.addCell(Cell(2,0)))
        assert(!b.addCell(Cell(0,1)))
        assert(!b.addCell(Cell(1,1)))
        assert(!b.addCell(Cell(2,1)))
        assert(!b.addCell(Cell(0,2)))
        assert(!b.addCell(Cell(1,2)))
        assert(!b.addCell(Cell(2,2)))

        assert(!b.addCell(Cell(-1, 0)))
        assert(!b.addCell(Cell(0, -1)))
        assert(!b.addCell(Cell(3, 0)))
        assert(!b.addCell(Cell(0, 3)))
    }
}
