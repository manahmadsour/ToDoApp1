package com.example.todoapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val tasks: List<Task>,
    private val onEditClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit,
    private val onCompleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskTextView: TextView = itemView.findViewById(R.id.taskTextView)
        private val endDateTextView: TextView = itemView.findViewById(R.id.endDateTextView)
        private val editButton: Button = itemView.findViewById(R.id.editButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        private val completeButton: Button = itemView.findViewById(R.id.completeButton)

        fun bind(task: Task) {
            taskTextView.text = "${task.name}   Priority: ${task.priority}"
            endDateTextView.text = "End Date: ${task.endDate}"

            // Change background color if the task is completed
            itemView.setBackgroundResource(
                if (task.completed) R.drawable.completed_bg
                else R.drawable.task_card_bg
            )

            completeButton.text = if (task.completed) "Completed" else "Mark Complete"

            editButton.setOnClickListener { onEditClick(task) }
            deleteButton.setOnClickListener { onDeleteClick(task) }
            completeButton.setOnClickListener { onCompleteClick(task) }

            // Share button functionality
            val shareButton: Button = itemView.findViewById(R.id.shareButton)
            shareButton.setOnClickListener {
                val shareText = "Task: ${task.name}\nPriority: ${task.priority}\nEnd Date: ${task.endDate}"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                // Launch the share intent
                itemView.context.startActivity(Intent.createChooser(intent, "Share Task"))
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun getItemCount() = tasks.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }


}
