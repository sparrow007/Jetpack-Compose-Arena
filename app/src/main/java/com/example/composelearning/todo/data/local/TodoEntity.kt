package com.example.composelearning.todo.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.composelearning.todo.domain.SyncState
import com.example.composelearning.todo.domain.Todo

@Entity(
    tableName = "todos",
    indices = [
        // The three columns every filter touches. Without these, the period query
        // degrades to a full table scan once the list grows.
        Index(value = ["remote_id"], unique = true),
        Index(value = ["is_done"]),
        Index(value = ["start_date", "end_date"]),
    ],
)
data class TodoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_id") val localId: Long = 0L,
    @ColumnInfo(name = "remote_id") val remoteId: String? = null,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "start_date") val startDate: Long,
    @ColumnInfo(name = "end_date") val endDate: Long,
    @ColumnInfo(name = "is_done") val isDone: Boolean,
    @ColumnInfo(name = "sync_state") val syncState: SyncState,
    /** Bumped on every local edit; used to resolve last-writer-wins against the server. */
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)

fun TodoEntity.toDomain() = Todo(
    localId = localId,
    remoteId = remoteId,
    title = title,
    content = content,
    startDate = startDate,
    endDate = endDate,
    isDone = isDone,
    syncState = syncState,
)

fun Todo.toEntity(updatedAt: Long = System.currentTimeMillis()) = TodoEntity(
    localId = localId,
    remoteId = remoteId,
    title = title,
    content = content,
    startDate = startDate,
    endDate = endDate,
    isDone = isDone,
    syncState = syncState,
    updatedAt = updatedAt,
)
