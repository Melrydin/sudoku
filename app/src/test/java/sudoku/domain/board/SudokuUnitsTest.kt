package sudoku.domain.board

import org.junit.Assert.assertEquals
import org.junit.Test

class SudokuUnitsTest {
    @Test
    fun rowsPartitionTheBoardFromTopToBottom() {
        assertEquals((0..8).toList(), SudokuUnits.rows.first())
        assertEquals((72..80).toList(), SudokuUnits.rows.last())
        assertPartition(SudokuUnits.rows)
    }

    @Test
    fun columnsPartitionTheBoardFromLeftToRight() {
        assertEquals(listOf(0, 9, 18, 27, 36, 45, 54, 63, 72), SudokuUnits.columns.first())
        assertEquals(listOf(8, 17, 26, 35, 44, 53, 62, 71, 80), SudokuUnits.columns.last())
        assertPartition(SudokuUnits.columns)
    }

    @Test
    fun boxesPartitionTheBoardInReadingOrder() {
        assertEquals(listOf(0, 1, 2, 9, 10, 11, 18, 19, 20), SudokuUnits.boxes.first())
        assertEquals(listOf(60, 61, 62, 69, 70, 71, 78, 79, 80), SudokuUnits.boxes.last())
        assertPartition(SudokuUnits.boxes)
    }

    @Test
    fun combinedGroupsCoverExactlyThePeerRelationships() {
        assertEquals(27, SudokuUnits.all.size)
        assertEquals(SudokuUnits.rows.first(), SudokuUnits.all[0])
        assertEquals(SudokuUnits.columns.first(), SudokuUnits.all[9])
        assertEquals(SudokuUnits.boxes.first(), SudokuUnits.all[18])
        repeat(SudokuBoard.CELL_COUNT) { first ->
            repeat(SudokuBoard.CELL_COUNT) { second ->
                val shareUnit = SudokuUnits.all.any { first in it && second in it }
                assertEquals("Cells $first and $second", SudokuRules.arePeers(first, second), shareUnit)
            }
        }
    }

    private fun assertPartition(units: List<List<Int>>) {
        assertEquals(SudokuBoard.SIZE, units.size)
        units.forEach { assertEquals(SudokuBoard.SIZE, it.size) }
        assertEquals((0 until SudokuBoard.CELL_COUNT).toList(), units.flatten().sorted())
    }
}
