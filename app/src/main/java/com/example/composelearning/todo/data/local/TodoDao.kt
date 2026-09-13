package com.example.composelearning.todo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.composelearning.todo.domain.SyncState
import kotlinx.coroutines.flow.Flow

/**
 * Abstract class rather than an interface so [upsertFromServer] can carry a real body
 * under @Transaction - Room only guarantees transaction wrapping for concrete methods.
 */
@Dao
abstract class TodoDao {

    /**
     * Dynamic filter. [RawQuery] with `observedEntities` keeps the Flow reactive even
     * though the SQL is built at runtime - a plain @Query cannot express "these three
     * conditions, any subset of them" without a pile of `(:x IS NULL OR col = :x)` hacks.
     */
    @RawQuery(observedEntities = [TodoEntity::class])
    abstract fun observeFiltered(query: SupportSQLiteQuery): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE local_id = :localId")
    abstract suspend fun findByLocalId(localId: Long): TodoEntity?

    @Query("SELECT * FROM todos WHERE remote_id = :remoteId")
    abstract suspend fun findByRemoteId(remoteId: String): TodoEntity?

    /** The outbox: everything the server has not accepted yet, oldest first. */
    @Query("SELECT * FROM todos WHERE sync_state != 'SYNCED' ORDER BY updated_at ASC")
    abstract suspend fun pendingSync(): List<TodoEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insert(entity: TodoEntity): Long

    @Update
    abstract suspend fun update(entity: TodoEntity)

    @Query("DELETE FROM todos WHERE local_id = :localId")
    abstract suspend fun delete(localId: Long)

    @Query("UPDATE todos SET remote_id = :remoteId, sync_state = :state WHERE local_id = :localId")
    abstract suspend fun markSynced(localId: Long, remoteId: String, state: SyncState)

    /**
     * Merge a server page into the cache.
     *
     * Rows still waiting to be pushed are left alone - overwriting a PENDING_* row with
     * the server's stale copy is how offline edits silently disappear. Matching is done
     * on remote_id so a row created offline and later confirmed is updated, not duplicated.
     */
    @Transaction
    open suspend fun upsertFromServer(entities: List<TodoEntity>) {
        entities.forEach { incoming ->
            val existing = incoming.remoteId?.let { findByRemoteId(it) }
            when {
                existing == null -> insert(incoming)
                existing.syncState != SyncState.SYNCED -> Unit // local edit wins until pushed
                else -> update(incoming.copy(localId = existing.localId))
            }
        }
    }
}
