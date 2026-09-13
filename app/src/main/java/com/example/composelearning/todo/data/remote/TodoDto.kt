package com.example.composelearning.todo.data.remote

import com.example.composelearning.todo.data.local.TodoEntity
import com.example.composelearning.todo.domain.SyncState
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Exactly the server contract: {"title","content","startDate","endDate","isDone"}. */
@JsonClass(generateAdapter = true)
data class TodoDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "startDate") val startDate: Long,
    @Json(name = "endDate") val endDate: Long,
    @Json(name = "isDone") val isDone: Boolean,
)

fun TodoDto.toEntity() = TodoEntity(
    remoteId = id,
    title = title,
    content = content,
    startDate = startDate,
    endDate = endDate,
    isDone = isDone,
    syncState = SyncState.SYNCED,
)

fun TodoEntity.toDto() = TodoDto(
    id = remoteId,
    title = title,
    content = content,
    startDate = startDate,
    endDate = endDate,
    isDone = isDone,
)
