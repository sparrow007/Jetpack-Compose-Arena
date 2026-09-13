package com.lycorp.interview.model

import java.time.Instant

/**
 * Basic data model for todo.
 *
 * Immutable on purpose:
 *  - `val` instead of `var` so the UI layer cannot silently mutate state the
 *    ViewModel owns. Callers produce a changed instance with [copy].
 *  - `data class` + `var` would also give an unstable `hashCode()`/`equals()`,
 *    which breaks `DiffUtil.areContentsTheSame` (old and new would be the same
 *    object, so the diff would see no change).
 *  - `java.time.Instant` instead of `java.util.Date`: `Date` is mutable and
 *    legacy. Requires minSdk 26, or core library desugaring:
 *      compileOptions { coreLibraryDesugaringEnabled true }
 *      coreLibraryDesugaring "com.android.tools:desugar_jdk_libs:2.0.4"
 */
data class Todo(
    val id: Int,
    val title: String = "",
    val content: String = "",
    val startDate: Instant = Instant.now(),
    val endDate: Instant? = null,
    val isDone: Boolean = false
) {
    init {
        require(endDate == null || !endDate.isBefore(startDate)) {
            "endDate ($endDate) must not be before startDate ($startDate)"
        }
    }
}


@HiltViewModelclass TodoViewModel @Inject constructor(private val repo: TodoRepository,private val savedState: SavedStateHandle,) : ViewModel() {

// LAYER 2 - the filter is persisted as the SAME "q" string the server takes.private val query: Flow<TodoQuery> =savedState.getStateFlow(KEY_QUERY, TodoQuery.ALL.toServerQuery()).map(TodoQuery::parse).distinctUntilChanged()private val refreshing = MutableStateFlow(false)private val offline = MutableStateFlow(false)private val effects = Channel<TodoEffect>(Channel.BUFFERED)val effect = effects.receiveAsFlow()val state: StateFlow<TodoUiState> = combine(// flatMapLatest: a filter change cancels the previous DB subscription, so a// result for the OLD filter can never arrive after one for the new filter.query.flatMapLatest { q -> repo.observeTodos(q).map { q to it } },refreshing,offline,) { (q, todos), isRefreshing, isOffline ->TodoUiState(todos, q, isRefreshing, isOffline)}.stateIn(scope = viewModelScope,// LAYER 3 - see below.started = SharingStarted.WhileSubscribed(5_000),initialValue = TodoUiState(),)fun onIntent(intent: TodoIntent) = when (intent) {is TodoIntent.FilterChanged -> savedState[KEY_QUERY] = intent.query.toServerQuery()is TodoIntent.ToggleDone -> launch { repo.setDone(intent.localId, intent.isDone) }is TodoIntent.Create -> launch {repo.createTodo(intent.title, intent.content, intent.start, intent.end)if (offline.value) effects.send(TodoEffect.SavedOffline)}TodoIntent.Refresh -> refresh()}private companion object { const val KEY_QUERY = "todo_query" }}