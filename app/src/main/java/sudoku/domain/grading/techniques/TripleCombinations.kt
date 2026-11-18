package sudoku.domain.grading.techniques

internal fun combinationsOfThree(values: List<Int>): List<List<Int>> {
    val combinations: MutableList<List<Int>> = mutableListOf()

    for (first: Int in 0 until (values.size - 2)) {
        for (second: Int in (first + 1) until (values.size - 1)) {
            for (third: Int in (second + 1) until values.size) {
                combinations.add(listOf(values[first], values[second], values[third]))
            }
        }
    }
    return combinations
}
