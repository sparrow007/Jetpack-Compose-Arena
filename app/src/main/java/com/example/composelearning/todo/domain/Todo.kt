package com.example.composelearning.todo.domain

/**
 * Domain model. Deliberately free of Room / Retrofit annotations so the UI and the
 * business rules never depend on a storage or transport detail.
 *
 * [localId] is the primary key we own; it exists the moment the user hits "save",
 * even in flight mode. [remoteId] is assigned by the server and is null until the
 * create request has actually succeeded. Everything downstream (edit, delete,
 * dedupe on refresh) keys off [localId], which is what makes offline creation safe.
 */
data class Todo(
    val localId: Long = 0L,
    val remoteId: String? = null,
    val title: String,
    val content: String,
    val startDate: Long,
    val endDate: Long,
    val isDone: Boolean = false,
    val syncState: SyncState = SyncState.PENDING_CREATE,
)

enum class SyncState {
    /** Created locally, server does not know about it yet. */
    PENDING_CREATE,

    /** Exists on the server, local copy has unsent edits. */
    PENDING_UPDATE,

    /** Local and server agree. */
    SYNCED,
}
