package com.example.composelearning.todo.data.local

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.composelearning.todo.domain.TodoQuery

/**
 * Compiles a [TodoQuery] into the local equivalent of the server's `q` parameter.
 *
 * Bound arguments only - the values never get concatenated into the SQL string, so a
 * future free-text filter cannot turn into an injection. The clauses mirror the server
 * contract exactly: `startDate` is a lower bound, `endDate` an upper bound.
 */
fun TodoQuery.toSupportQuery(): SupportSQLiteQuery {
    val clauses = mutableListOf<String>()
    val args = mutableListOf<Any>()

    isDone?.let {
        clauses += "is_done = ?"
        args += if (it) 1 else 0
    }
    startDate?.let {
        clauses += "start_date >= ?"
        args += it
    }
    endDate?.let {
        clauses += "end_date <= ?"
        args += it
    }

    val where = if (clauses.isEmpty()) "" else "WHERE " + clauses.joinToString(" AND ")
    return SimpleSQLiteQuery(
        "SELECT * FROM todos $where ORDER BY start_date ASC, local_id ASC",
        args.toTypedArray(),
    )
}
