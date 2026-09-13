package com.lycorp.interview.client

import android.content.res.Resources
import com.lycorp.interview.R
import com.lycorp.interview.model.Todo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * A mock [TodoApiClient] to return mock [Todo].
 *
 * Changed from `object` + `attachContext()` to a class with constructor
 * injection because:
 *  - a singleton holding a `Context` outlives the Activity and leaks it;
 *  - `attachContext()` is temporal coupling: forget to call it and every
 *    string silently falls back to "" with no crash and no log;
 *  - a global mutable `var context` is not thread safe and leaks state
 *    between unit tests;
 *  - an `object` cannot be swapped for a fake in tests.
 *
 * Construct with `applicationContext.resources`, never an Activity's.
 */
class MockTodoApiClient(
    private val resources: Resources
) : TodoApiClient {

    // withContext(Dispatchers.IO): the real implementation would do blocking
    // socket work here, so the client - not the caller - owns the dispatcher.
    override suspend fun fetchTodos(): List<Todo> = withContext(Dispatchers.IO) {
        delay(SIMULATED_LATENCY_MS)
        listOf(
            Todo(
                id = 1,
                title = resources.getString(R.string.test_title1),
                content = resources.getString(R.string.test_content1)
            ),
            Todo(
                id = 2,
                title = resources.getString(R.string.test_title2),
                content = resources.getString(R.string.test_content2)
            )
        )
    }

    private companion object {
        const val SIMULATED_LATENCY_MS = 100L
    }
}
