package sudoku.domain.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OptionalValueTest {
    @Test
    fun zeroAndEmptyCollectionsRemainPresent() {
        val zero: OptionalValue<Int> = OptionalValue.Present(0)
        val empty: OptionalValue<Set<Int>> = OptionalValue.Present(emptySet())
        assertTrue(zero.isPresent)
        assertTrue(empty.isPresent)
        assertEquals(0, zero.get())
        assertEquals(emptySet<Int>(), empty.get())
    }

    @Test
    fun missingValuesSkipTransformationsAndPredicates() {
        val missing: OptionalValue<Int> = OptionalValue.Absent
        assertEquals(OptionalValue.Absent, missing.map<Int> { error("Unexpected transformation.") })
        assertEquals(OptionalValue.Absent, missing.flatMap<Int> { error("Unexpected transformation.") })
        assertFalse(missing.matches { error("Unexpected predicate.") })
    }

    @Test
    fun fallbackIsEvaluatedOnlyForMissingValues() {
        var calls: Int = 0
        val present: OptionalValue<Int> = OptionalValue.Present(0)
        val missing: OptionalValue<Int> = OptionalValue.Absent
        assertEquals(0, present.getOrElse { calls++; 42 })
        assertEquals(0, calls)
        assertEquals(42, missing.getOrElse { calls++; 42 })
        assertEquals(1, calls)
    }

    @Test
    fun mapsAndComposesPresentValues() {
        val value: OptionalValue<Int> = OptionalValue.Present(4)
        val mapped: OptionalValue<Int> = value.map { number: Int -> number + 1 }
        val composed: OptionalValue<Int> = mapped.flatMap { number: Int -> OptionalValue.Present(number * 2) }
        assertEquals(OptionalValue.Present(10), composed)
        assertEquals(OptionalValue.Absent, value.flatMap { OptionalValue.Absent })
    }

    @Test
    fun filtersWithoutCreatingADummyValue() {
        val value: OptionalValue<Int> = OptionalValue.Present(0)
        assertEquals(value, value.filter { number: Int -> number == 0 })
        assertEquals(OptionalValue.Absent, value.filter { number: Int -> number > 0 })
        assertEquals(listOf(0), value.asList())
        assertEquals(emptyList<Int>(), OptionalValue.Absent.asList())
    }

    @Test
    fun foldsAndRunsActionsOnlyForTheSelectedBranch() {
        val value: OptionalValue<Int> = OptionalValue.Present(7)
        var recorded: Int = 0
        value.ifPresent { number: Int -> recorded = number }
        OptionalValue.Absent.ifPresent { error("Unexpected action.") }
        assertEquals(7, recorded)
        assertEquals(7, value.fold({ error("Unexpected fallback.") }) { number: Int -> number })
        assertEquals(9, OptionalValue.Absent.fold({ 9 }) { error("Unexpected value.") })
    }

    @Test
    fun convertsNullableBoundaryValuesToExplicitAbsence() {
        assertEquals(OptionalValue.Absent, OptionalValue.fromNullable<String>(null))
        assertEquals(OptionalValue.Present(""), OptionalValue.fromNullable(""))
    }

    @Test(expected = NoSuchElementException::class)
    fun readingAnAbsentValueFailsExplicitly() {
        OptionalValue.Absent.get()
    }
}
