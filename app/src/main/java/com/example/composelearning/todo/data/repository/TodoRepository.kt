package com.example.composelearning.todo.data.repository

import com.example.composelearning.todo.domain.Todo
import com.example.composelearning.todo.domain.TodoQuery
import kotlinx.coroutines.flow.Flow

interface TodoRepository {

    /**
     * The single source of truth for the UI: always the local database, so the list
     * renders in flight mode and re-renders by itself when a sync lands.
     */
    fun observeTodos(query: TodoQuery): Flow<List<Todo>>

    /**
     * Writes locally first and returns immediately, then schedules the upload.
     * Returns the local id so the caller can navigate to the row it just created,
     * network or not.
     */
    suspend fun createTodo(
        title: String,
        content: String,
        startDate: Long,
        endDate: Long,
    ): Long

    suspend fun setDone(localId: Long, isDone: Boolean)

    /** Pull the given slice from the server into the cache. */
    suspend fun refresh(query: TodoQuery): Result<Unit>

    /** Drain the outbox. Called by the sync worker, and on app start. */
    suspend fun pushPending(): Result<Unit>
}
