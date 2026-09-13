package com.lycorp.interview

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lycorp.interview.client.MockTodoApiClient
import com.lycorp.interview.ui.TodoAdapter
import com.lycorp.interview.viewmodel.TodoUiState
import com.lycorp.interview.viewmodel.TodoViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // applicationContext.resources, not the Activity's: the client must not
    // hold anything that outlives this screen.
    private val viewModel: TodoViewModel by viewModels {
        TodoViewModel.Factory(MockTodoApiClient(applicationContext.resources))
    }

    // One adapter for the life of the Activity; the list arrives via submitList().
    private val todoAdapter = TodoAdapter { id, isDone ->
        viewModel.setDone(id, isDone)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val todoList: RecyclerView = findViewById(R.id.todo_list)
        todoList.layoutManager = LinearLayoutManager(this)
        todoList.adapter = todoAdapter

        // lifecycleScope alone cancels only on DESTROY, so collection - and the
        // UI writes it triggers - would keep running while the Activity is in
        // the background. repeatOnLifecycle(STARTED) starts collection when the
        // Activity becomes visible and cancels it on STOP.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: TodoUiState) {
        when (state) {
            is TodoUiState.Loading -> Unit // wire up a progress indicator here
            is TodoUiState.Success -> todoAdapter.submitList(state.todos)
            is TodoUiState.Error -> Toast.makeText(
                this,
                getString(R.string.error_load_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
