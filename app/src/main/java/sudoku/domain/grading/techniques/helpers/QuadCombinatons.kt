package sudoku.domain.grading.techniques.helpers

internal fun combinationsOfFour(values: List<Int>): List<List<Int>> {
    val combinations: MutableList<List<Int>> = mutableListOf()

    for (first: Int in 0 until (values.size - 3)) {
        for (second: Int in (first + 1) until (values.size - 2)) {
            for (third: Int in (second + 1) until (values.size - 1)) {
                for (four: Int in (third + 1) until values.size) {
                    combinations.add(
                        listOf(
                            values[first], values[second], values[third], values[four]
                        )
                    )
                }
            }
        }
    }
    return combinations
}
