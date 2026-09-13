package com.lycorp.interview.client

import com.lycorp.interview.model.Todo

/**
 * Contract for loading todos.
 *
 * The error path is part of the contract, so it is documented here instead of
 * being discovered at crash time by the caller.
 */
interface TodoApiClient {

    /**
     * @throws java.io.IOException on network failure.
     * @throws java.util.concurrent.TimeoutException when the request times out.
     */
    suspend fun fetchTodos(): List<Todo>
}
