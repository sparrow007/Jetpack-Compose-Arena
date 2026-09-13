package com.example.composelearning.todo.data.local

import androidx.room.TypeConverter
import com.example.composelearning.todo.domain.SyncState

class Converters {
    @TypeConverter
    fun fromSyncState(state: SyncState): String = state.name

    @TypeConverter
    fun toSyncState(raw: String): SyncState =
        runCatching { SyncState.valueOf(raw) }.getOrDefault(SyncState.SYNCED)
}
