package sudoku.domain.generation

import org.junit.Assert.assertTrue
import org.junit.Test
import sudoku.domain.board.SudokuRules
import kotlin.random.Random

class SudokuGeneratorTest {
    @Test(timeout = 5000)
    fun differentSeedsProduceValidSolutionsFromAnEmptyBoard() {
        repeat(20) { seed ->
            val generator = SudokuGenerator(Random(seed))
            assertTrue(SudokuRules.isComplete(generator.generate()))
            assertTrue(generator.freeCells.isEmpty())
        }
    }
}
