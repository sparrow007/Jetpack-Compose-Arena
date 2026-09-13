package com.example.composelearning.todo.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.composelearning.todo.data.repository.TodoRepository
import com.example.composelearning.todo.domain.TodoQuery
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TodoSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: TodoRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Push before pull, so a todo created offline is not clobbered by a server page
        // that predates it.
        val pushed = repository.pushPending()
        if (pushed.isFailure) return Result.retry()

        return if (repository.refresh(TodoQuery.ALL).isSuccess) Result.success()
        else Result.retry()
    }
}
