package com.lycorp.interview.viewmodel

import com.lycorp.interview.model.Todo

/**
 * A bare `List<Todo>` cannot tell the UI whether an empty list means
 * "still loading", "loaded, nothing to show" or "the request failed".
 * This sealed hierarchy makes those three cases explicit and makes the
 * `when` in the Activity exhaustive.
 */
sealed interface TodoUiState {

    data object Loading : TodoUiState

    data class Success(val todos: List<Todo>) : TodoUiState

    data class Error(val cause: Throwable) : TodoUiState
}
