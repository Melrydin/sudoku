package sudoku.domain.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import sudoku.domain.board.SudokuBoard
import sudoku.domain.board.SudokuRules
import sudoku.fixtures.TestBoards

class SudokuGeneratorBacktrackingTest {
    private val puzzle = SudokuBoard.parse(
        "534608012602100040098342560059060023426053001010924856901037084280000005045080179",
    )

    @Test(timeout = 5000)
    fun recoversFromDeadEndsWithoutChangingStartingNumbers() {
        val generator = generatorFor(puzzle)
        val result = generator.generate()
        assertTrue(SudokuRules.isComplete(result))
        assertTrue(generator.freeCells.isEmpty())
        assertTrue(puzzle.cells.indices.all { puzzle[it] == 0 || puzzle[it] == result[it] })
    }

    @Test(timeout = 5000)
    fun freshGeneratorsRepeatTheSameChoices() {
        assertEquals(generatorFor(puzzle).generate().cells, generatorFor(puzzle).generate().cells)
    }

    @Test(timeout = 5000)
    fun exhaustedSearchRestoresTheStartingBoardAndFreeCells() {
        val blockedCells = setOf(7, 18) + (72..80)
        val blocked = blockedCells.fold(TestBoards.solved().withValue(0, 1)) { board, cell ->
            board.withValue(cell, 0)
        }
        assertTrue(SudokuRules.conflicts(blocked).isEmpty())
        val generator = generatorFor(blocked)
        assertThrows(IllegalStateException::class.java) { generator.generate() }
        assertEquals(blocked.cells, generator.board.cells)
        assertEquals(blockedCells, generator.freeCells.toSet())
        assertEquals(blockedCells.size, generator.freeCells.size)
    }

    @Test
    fun rejectsAFilledBoardWithConflicts() {
        val generator = generatorFor(TestBoards.solved().withValue(0, 1))
        assertThrows(IllegalStateException::class.java) { generator.generate() }
    }

    private fun generatorFor(puzzle: SudokuBoard): SudokuGenerator {
        return SudokuGenerator().apply {
            board = puzzle
            freeCells = puzzle.cells.indices.filter { puzzle[it] == 0 }.toMutableList()
        }
    }
}
