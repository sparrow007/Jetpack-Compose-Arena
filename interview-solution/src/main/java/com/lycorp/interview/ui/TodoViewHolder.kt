package com.lycorp.interview.ui

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lycorp.interview.R
import com.lycorp.interview.model.Todo

/**
 * Views are private and binding lives in [bind], so the adapter no longer
 * reaches into the holder's widgets.
 *
 * Prefer ViewBinding over findViewById once it is enabled on the module:
 *   `TodoViewHolder(ItemTodoBinding.inflate(inflater, parent, false))`
 */
internal class TodoViewHolder(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    private val title: TextView = itemView.findViewById(R.id.title)
    private val content: TextView = itemView.findViewById(R.id.content)
    private val isDone: CheckBox = itemView.findViewById(R.id.done)

    fun bind(todo: Todo, onDoneChanged: (id: Int, isDone: Boolean) -> Unit) {
        title.text = todo.title
        content.text = todo.content

        // Detach before writing the state. A recycled holder still carries the
        // listener bound for the previous row, and setChecked() would fire it,
        // flipping isDone on the wrong Todo.
        isDone.setOnCheckedChangeListener(null)
        isDone.isChecked = todo.isDone
        isDone.setOnCheckedChangeListener { _, checked ->
            onDoneChanged(todo.id, checked)
        }
    }
}
