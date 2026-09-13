package com.lycorp.interview.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lycorp.interview.client.TodoApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.TimeoutException

class TodoViewModel(
    private val apiClient: TodoApiClient
) : ViewModel() {

    private val mutableUiState = MutableStateFlow<TodoUiState>(TodoUiState.Loading)

    // asStateFlow() so a consumer cannot cast this back to MutableStateFlow
    // and write into it. The ViewModel stays the single writer.
    val uiState: StateFlow<TodoUiState> = mutableUiState.asStateFlow()

    private var refreshJob: Job? = null

    init {
        // The original code never called refreshTodos(), so the list stayed empty.
        refreshTodos()
    }

    fun refreshTodos() {
        // Without this guard, repeated calls start overlapping requests and a
        // stale response can land last and win.
        if (refreshJob?.isActive == true) return

        refreshJob = viewModelScope.launch {
            mutableUiState.value = TodoUiState.Loading
            // fetchTodos() may throw IOException / TimeoutException. Uncaught,
            // it escapes viewModelScope and crashes the app.
            mutableUiState.value = try {
                TodoUiState.Success(apiClient.fetchTodos())
            } catch (e: IOException) {
                TodoUiState.Error(e)
            } catch (e: TimeoutException) {
                TodoUiState.Error(e)
            }
        }
    }

    /**
     * Single source of truth for the done flag.
     *
     * The adapter used to write `todoItem.isDone = isChecked` directly on the
     * model. The ViewModel never learned about it, no new value was emitted,
     * and the change was lost on the next refresh or configuration change.
     */
    fun setDone(id: Int, isDone: Boolean) {
        val current = mutableUiState.value as? TodoUiState.Success ?: return
        if (current.todos.none { it.id == id && it.isDone != isDone }) return

        mutableUiState.value = TodoUiState.Success(
            current.todos.map { todo ->
                if (todo.id == id) todo.copy(isDone = isDone) else todo
            }
        )
    }

    /**
     * `by viewModels()` cannot build a ViewModel that has constructor
     * arguments; without a factory it fails at runtime. Replace with Hilt
     * (`@HiltViewModel`) once DI is introduced.
     */
    class Factory(
        private val apiClient: TodoApiClient
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TodoViewModel::class.java)) {
                "Unknown ViewModel class: $modelClass"
            }
            return TodoViewModel(apiClient) as T
        }
    }
}
