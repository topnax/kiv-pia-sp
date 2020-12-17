package com.zcu.kiv.pia.tictactoe.game

import org.junit.Test

class BoardCheckerTest {

    @Test
    fun testGetCellsWithSameSeed() {
        val b = Board(3, 3)
        val checker = SimpleBoardChecker(b, 3)

        b.addCell(Cell(0, 0).apply { value = Seed.CROSS })
        b.addCell(Cell(1, 1).apply { value = Seed.CROSS })
        b.addCell(Cell(2, 2).apply { value = Seed.CROSS })

        assert(checker.getCellsWithSameSeed(Cell(0, 0).apply { value = Seed.CROSS }, 1, 1).size == 2)
        assert(checker.getCellsWithSameSeed(Cell(2, 2).apply { value = Seed.CROSS }, -1, -1).size == 2)
    }

    @Test
    fun testIsTurnVictoriousDiagonal() {
        val b = Board(3, 3)
        val checker = SimpleBoardChecker(b, 3)
        b.addCell(Cell(0, 0).apply { value = Seed.CROSS })
        b.addCell(Cell(1, 1).apply { value = Seed.CROSS })
        b.addCell(Cell(2, 2).apply { value = Seed.CROSS })
        b.addCell(Cell(0, 1).apply { value = Seed.NOUGHT })
        b.addCell(Cell(0, 2).apply { value = Seed.NOUGHT })
        b.addCell(Cell(1, 0).apply { value = Seed.NOUGHT })
        b.addCell(Cell(2, 0).apply { value = Seed.NOUGHT })

        b.addCell(Cell(2, 1).apply { value = Seed.NOUGHT })
        b.addCell(Cell(1, 2).apply { value = Seed.NOUGHT })

        val victoriousCells = setOf(b[0, 0], b[1, 1], b[2, 2])

        assert(checker.isCellVictorious(0, 0))
        assert(checker.getVictriousCells() == victoriousCells)

        assert(checker.isCellVictorious(1, 1))
        assert(checker.getVictriousCells() == victoriousCells)

        assert(checker.isCellVictorious(2, 2))
        assert(checker.getVictriousCells() == victoriousCells)

        assert(!checker.isCellVictorious(0, 1))
        assert(!checker.isCellVictorious(0, 2))
        assert(!checker.isCellVictorious(1, 0))
        assert(!checker.isCellVictorious(1, 2))
        assert(!checker.isCellVictorious(2, 0))
        assert(!checker.isCellVictorious(2, 1))
    }

    @Test
    fun testIsTurnVictoriousHorizontal() {
        val b = Board(5, 5)
        val checker = SimpleBoardChecker(b, 4)
        b.addCell(Cell(0, 1).apply { value = Seed.CROSS })
        b.addCell(Cell(2, 2).apply { value = Seed.CROSS })
        b.addCell(Cell(2, 0).apply { value = Seed.CROSS })

        b.addCell(Cell(0, 0).apply { value = Seed.NOUGHT })
        b.addCell(Cell(1, 0).apply { value = Seed.NOUGHT })
        b.addCell(Cell(1, 1).apply { value = Seed.NOUGHT })
        b.addCell(Cell(1, 2).apply { value = Seed.NOUGHT })
        b.addCell(Cell(1, 3).apply { value = Seed.NOUGHT })

        val victoriousCells = setOf(b[1, 0], b[1, 1], b[1, 2], b[1, 3])

        assert(checker.isCellVictorious(1, 0))
        assert(checker.getVictriousCells() == victoriousCells)

        assert(checker.isCellVictorious(1, 1))
        assert(checker.getVictriousCells() == victoriousCells)

        assert(checker.isCellVictorious(1, 2))
        assert(checker.getVictriousCells() == victoriousCells)

        assert(checker.isCellVictorious(1, 3))
        assert(checker.getVictriousCells() == victoriousCells)

        assert(!checker.isCellVictorious(-1, 0))
        assert(!checker.isCellVictorious(0, -1))
        assert(!checker.isCellVictorious(-1, -1))
        assert(!checker.isCellVictorious(0, 0))
        assert(!checker.isCellVictorious(0, 2))
        assert(!checker.isCellVictorious(3, 0))
        assert(!checker.isCellVictorious(1, 4))
        assert(!checker.isCellVictorious(2, 1))
    }
}
