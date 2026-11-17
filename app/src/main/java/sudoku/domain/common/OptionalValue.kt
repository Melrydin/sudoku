package sudoku.domain.common

sealed class OptionalValue<out T : Any> {
    data class Present<out T : Any>(val value: T) : OptionalValue<T>()
    data object Absent : OptionalValue<Nothing>()

    val isPresent: Boolean get() = this is Present

    fun get(): T = getOrElse { throw NoSuchElementException("No value present.") }

    inline fun getOrElse(defaultValue: () -> @UnsafeVariance T): T = when (this) {
        is Present -> value
        Absent -> defaultValue()
    }

    inline fun <R : Any> fold(ifAbsent: () -> R, ifPresent: (T) -> R): R = when (this) {
        is Present -> ifPresent(value)
        Absent -> ifAbsent()
    }

    inline fun <R : Any> map(transform: (T) -> R): OptionalValue<R> = when (this) {
        is Present -> Present(transform(value))
        Absent -> Absent
    }

    inline fun <R : Any> flatMap(transform: (T) -> OptionalValue<R>): OptionalValue<R> = when (this) {
        is Present -> transform(value)
        Absent -> Absent
    }

    inline fun matches(predicate: (T) -> Boolean): Boolean = this is Present && predicate(value)

    inline fun filter(predicate: (T) -> Boolean): OptionalValue<T> =
        if (matches(predicate)) this else Absent

    inline fun ifPresent(action: (T) -> Unit) {
        if (this is Present) action(value)
    }

    fun asList(): List<T> = fold({ emptyList() }) { value: T -> listOf(value) }

    companion object {
        fun <T : Any> fromNullable(value: T?): OptionalValue<T> =
            if (value == null) Absent else Present(value)
    }
}
