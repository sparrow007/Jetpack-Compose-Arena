package com.example.composelearning.todo.domain

/**
 * A single description of "which todos do I want", used for BOTH sides of the app:
 *
 *  - [toServerQuery] renders the server's `?q=isDone:true+startDate:1+endDate:2` syntax.
 *  - the Room layer compiles the same object into a WHERE clause (see TodoDao/TodoQuerySql).
 *
 * Keeping one source of truth for the predicate is what stops the classic offline-first
 * bug where the cached list and the network list disagree about what the filter meant.
 */
data class TodoQuery(
    val isDone: Boolean? = null,
    /** Inclusive lower bound matched against [Todo.startDate]. */
    val startDate: Long? = null,
    /** Inclusive upper bound matched against [Todo.endDate]. */
    val endDate: Long? = null,
) {
    fun toServerQuery(): String? =
        buildList {
            isDone?.let { add("isDone:$it") }
            startDate?.let { add("startDate:$it") }
            endDate?.let { add("endDate:$it") }
        }.takeIf { it.isNotEmpty() }?.joinToString("+")

    companion object {
        val ALL = TodoQuery()
        val UNFINISHED = TodoQuery(isDone = false)

        fun inPeriod(from: Long, to: Long) = TodoQuery(startDate = from, endDate = to)

        /** Inverse of [toServerQuery]; handy for deep links and for tests. */
        fun parse(raw: String?): TodoQuery {
            if (raw.isNullOrBlank()) return ALL
            var query = ALL
            raw.split('+').forEach { term ->
                val key = term.substringBefore(':', missingDelimiterValue = "")
                val value = term.substringAfter(':', missingDelimiterValue = "")
                query = when (key) {
                    "isDone" -> query.copy(isDone = value.toBooleanStrictOrNull())
                    "startDate" -> query.copy(startDate = value.toLongOrNull())
                    "endDate" -> query.copy(endDate = value.toLongOrNull())
                    else -> query // unknown key: ignore rather than crash on a future server field
                }
            }
            return query
        }
    }
}
