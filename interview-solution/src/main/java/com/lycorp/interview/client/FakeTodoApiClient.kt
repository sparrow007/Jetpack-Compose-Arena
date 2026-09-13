package com.lycorp.interview.client

import com.lycorp.interview.model.Todo

/**
 * Pure-JVM [TodoApiClient] for unit tests.
 *
 * [MockTodoApiClient] reads from `R.string`, so it needs the Android framework
 * and cannot run in a plain `src/test` JVM test. This one has no Android
 * dependency and can also be told to fail, so the error branch of the
 * ViewModel is testable.
 */
class FakeTodoApiClient(
    private val todos: List<Todo> = emptyList(),
    private val error: Throwable? = null
) : TodoApiClient {

    override suspend fun fetchTodos(): List<Todo> {
        error?.let { throw it }
        return todos
    }
}
