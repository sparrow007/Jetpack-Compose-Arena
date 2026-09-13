package com.example.composelearning.todo.data.repository

import com.example.composelearning.todo.data.local.TodoDao
import com.example.composelearning.todo.data.local.TodoEntity
import com.example.composelearning.todo.data.local.toDomain
import com.example.composelearning.todo.data.local.toSupportQuery
import com.example.composelearning.todo.data.remote.TodoApi
import com.example.composelearning.todo.data.remote.toDto
import com.example.composelearning.todo.data.remote.toEntity
import com.example.composelearning.todo.data.sync.SyncScheduler
import com.example.composelearning.todo.di.IoDispatcher
import com.example.composelearning.todo.domain.SyncState
import com.example.composelearning.todo.domain.Todo
import com.example.composelearning.todo.domain.TodoQuery
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class DefaultTodoRepository @Inject constructor(
    private val dao: TodoDao,
    private val api: TodoApi,
    private val syncScheduler: SyncScheduler,
    @IoDispatcher private val io: CoroutineDispatcher,
) : TodoRepository {

    override fun observeTodos(query: TodoQuery): Flow<List<Todo>> =
        dao.observeFiltered(query.toSupportQuery())
            .map { rows -> rows.map(TodoEntity::toDomain) }
            .flowOn(io)

    override suspend fun createTodo(
        title: String,
        content: String,
        startDate: Long,
        endDate: Long,
    ): Long = withContext(io) {
        val localId = dao.insert(
            TodoEntity(
                title = title,
                content = content,
                startDate = startDate,
                endDate = endDate,
                isDone = false,
                syncState = SyncState.PENDING_CREATE,
            ),
        )
        // The row is already visible to the UI at this point. The upload is a separate,
        // retryable concern - the user is never made to wait on the radio.
        syncScheduler.requestSync()
        localId
    }

    override suspend fun setDone(localId: Long, isDone: Boolean) = withContext(io) {
        val current = dao.findByLocalId(localId) ?: return@withContext
        dao.update(
            current.copy(
                isDone = isDone,
                updatedAt = System.currentTimeMillis(),
                // A row that was never created upstream stays PENDING_CREATE; otherwise
                // it becomes an update to push.
                syncState = if (current.remoteId == null) SyncState.PENDING_CREATE
                else SyncState.PENDING_UPDATE,
            ),
        )
        syncScheduler.requestSync()
    }

    override suspend fun refresh(query: TodoQuery): Result<Unit> = withContext(io) {
        runCatching {
            val remote = api.getTodos(query.toServerQuery())
            dao.upsertFromServer(remote.map { it.toEntity() })
        }.recoverCatching { error ->
            // Offline is an expected state here, not a bug: the cache already served
            // the UI, so we surface the failure without clearing anything.
            if (error is IOException) throw OfflineException(error) else throw error
        }
    }

    override suspend fun pushPending(): Result<Unit> = withContext(io) {
        runCatching {
            dao.pendingSync().forEach { pending ->
                when (pending.syncState) {
                    SyncState.PENDING_CREATE -> {
                        val created = api.create(pending.toDto())
                        val remoteId = created.id
                            ?: error("Server accepted the todo but returned no id")
                        dao.markSynced(pending.localId, remoteId, SyncState.SYNCED)
                    }
                    // PUT/PATCH is not in the given server contract yet; the outbox is
                    // already shaped for it so adding the call is a one-line change.
                    SyncState.PENDING_UPDATE -> Unit
                    SyncState.SYNCED -> Unit
                }
            }
        }
    }
}

class OfflineException(cause: Throwable) : IOException("No network", cause)
