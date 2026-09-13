package com.lycorp.interview.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.lycorp.interview.R
import com.lycorp.interview.model.Todo

/**
 * [ListAdapter] instead of a plain `RecyclerView.Adapter` that takes the list
 * in its constructor.
 *
 * The old code built `TodoAdapter(todos)` on every emission and assigned it to
 * the RecyclerView: that throws away view recycling and scroll position and
 * rebinds every row. Here the Activity creates one adapter and calls
 * `submitList()`, so DiffUtil updates only the rows that actually changed.
 *
 * [onDoneChanged] reports the checkbox back to the ViewModel rather than
 * letting the adapter mutate the model.
 */
internal class TodoAdapter(
    private val onDoneChanged: (id: Int, isDone: Boolean) -> Unit
) : ListAdapter<Todo, TodoViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(getItem(position), onDoneChanged)
    }

    private companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Todo>() {

            override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean =
                oldItem.id == newItem.id

            // Works only because Todo is an immutable data class. With `var`
            // fields the old item would be the same instance as the new one
            // and this would always report "no change".
            override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean =
                oldItem == newItem
        }
    }
}
