package sudoku.domain.generation

import kotlin.random.Random
import org.junit.Assert.assertTrue
import org.junit.Test
import sudoku.domain.board.SudokuRules

class SudokuGeneratorTest {
    @Test(timeout = 5000)
    fun differentSeedsProduceValidSolutionsFromAnEmptyBoard() {
        repeat(20) { seed: Int ->
            val generator: SudokuGenerator = SudokuGenerator(Random(seed))
            assertTrue(SudokuRules.isComplete(generator.generate()))
            assertTrue(generator.freeCells.isEmpty())
        }
    }
}
