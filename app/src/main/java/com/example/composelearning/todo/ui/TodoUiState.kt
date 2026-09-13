package com.example.composelearning.todo.ui

import com.example.composelearning.todo.domain.Todo
import com.example.composelearning.todo.domain.TodoQuery

data class TodoUiState(
    val todos: List<Todo> = emptyList(),
    val query: TodoQuery = TodoQuery.ALL,
    val isRefreshing: Boolean = false,
    /** True when the last refresh failed on the network; the list below is still valid. */
    val isOffline: Boolean = false,
)

/** One-shot events. Kept out of [TodoUiState] so a rotation cannot replay a snackbar. */
sealed interface TodoEvent {
    data object SavedOffline : TodoEvent
    data class Error(val message: String) : TodoEvent
}
