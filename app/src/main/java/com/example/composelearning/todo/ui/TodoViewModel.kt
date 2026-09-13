package com.example.composelearning.todo.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composelearning.todo.data.repository.OfflineException
import com.example.composelearning.todo.data.repository.TodoRepository
import com.example.composelearning.todo.domain.TodoQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /**
     * Rotation handling, layer 1: the ViewModel outlives the Activity, so the list and
     * the in-flight refresh are simply not re-created.
     *
     * Layer 2: the filter lives in [SavedStateHandle], serialised as the very same
     * `q` string the server takes. That survives process death (Don't Keep Activities,
     * low-memory kill), not just a rotation, and costs one String in the saved bundle.
     */
    private val queryFlow = savedStateHandle.getStateFlow(KEY_QUERY, TodoQuery.ALL.toServerQuery())
        .map(TodoQuery::parse)
        .distinctUntilChanged()

    private val refreshing = MutableStateFlow(false)
    private val offline = MutableStateFlow(false)

    private val events = Channel<TodoEvent>(Channel.BUFFERED)
    val events2 = events.receiveAsFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState = combine(
        queryFlow.flatMapLatest { query ->
            // flatMapLatest: changing the filter cancels the previous DB subscription,
            // so an old result can never arrive after a newer one.
            repository.observeTodos(query).map { query to it }
        },
        refreshing,
        offline,
    ) { (query, todos), isRefreshing, isOffline ->
        TodoUiState(todos = todos, query = query, isRefreshing = isRefreshing, isOffline = isOffline)
    }.stateIn(
        scope = viewModelScope,
        // 5s grace period: a rotation unsubscribes and resubscribes within milliseconds,
        // so the upstream DB query is not torn down and rebuilt for nothing.
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodoUiState(),
    )

    init {
        refresh()
    }

    fun setQuery(query: TodoQuery) {
        savedStateHandle[KEY_QUERY] = query.toServerQuery()
        refresh()
    }

    fun showUnfinishedOnly(enabled: Boolean) =
        setQuery(uiState.value.query.copy(isDone = if (enabled) false else null))

    fun setPeriod(from: Long?, to: Long?) =
        setQuery(uiState.value.query.copy(startDate = from, endDate = to))

    fun createTodo(title: String, content: String, startDate: Long, endDate: Long) {
        viewModelScope.launch {
            repository.createTodo(title, content, startDate, endDate)
            // The row is on screen already; tell the user only when it is not yet upstream.
            if (offline.value) events.send(TodoEvent.SavedOffline)
        }
    }

    fun setDone(localId: Long, isDone: Boolean) {
        viewModelScope.launch { repository.setDone(localId, isDone) }
    }

    fun refresh() {
        viewModelScope.launch {
            refreshing.value = true
            repository.refresh(uiState.value.query)
                .onSuccess { offline.value = false }
                .onFailure { error ->
                    offline.value = error is OfflineException
                    if (error !is OfflineException) {
                        events.send(TodoEvent.Error(error.message ?: "Refresh failed"))
                    }
                }
            refreshing.value = false
        }
    }

    private companion object {
        const val KEY_QUERY = "todo_query"
    }
}
