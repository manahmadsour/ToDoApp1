package com.example.todoapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TasksActivity : AppCompatActivity() {
    private lateinit var taskRecyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var searchView: SearchView
    private lateinit var categoryTitle: TextView
    private val db = FirebaseFirestore.getInstance()
    private val tasks = mutableListOf<Task>()
    private lateinit var categoryId: String
    private lateinit var userid: String
    private lateinit var title: String
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        categoryId = intent.getStringExtra("categoryId") ?: ""
        title = intent.getStringExtra("title") ?: ""
        userid = intent.getStringExtra("UID") ?: ""
        taskRecyclerView = findViewById(R.id.taskRecyclerView)
        searchView = findViewById(R.id.searchView)
        categoryTitle = findViewById(R.id.categoryTitle)

        taskAdapter = TaskAdapter(tasks,
            onEditClick = { task -> showEditTaskDialog(task) },
            onDeleteClick = { task -> deleteTask(task) },
            onCompleteClick = { task -> completeTask(task) }
        )
        taskRecyclerView.layoutManager = LinearLayoutManager(this)
        taskRecyclerView.adapter = taskAdapter

        fetchTasks()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchTasks(it) }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchTasks(it) }
                return false
            }
        })

        findViewById<View>(R.id.addTaskButton).setOnClickListener {
            showAddTaskDialog()
        }
        findViewById<View>(R.id.backButton).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("UID", userid)
            startActivity(intent)
        }
        findViewById<View>(R.id.logoutIcon).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }


    private fun fetchTasks() {
        db.collection("tasks")
            .whereEqualTo("categoryId", categoryId)
            .get()
            .addOnSuccessListener { result ->
                tasks.clear()
                for (document in result) {

                    val task = document.toObject(Task::class.java)

                    task.id = document.id

                    tasks.add(task)
                }
                // Notify the adapter that the data has changed
                taskAdapter.notifyDataSetChanged()
            }
    }


    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val prioritySpinner = dialogView.findViewById<Spinner>(R.id.prioritySpinner)
        setupPrioritySpinner(prioritySpinner)

        val endDateEditText = dialogView.findViewById<EditText>(R.id.endDateEditText)
        endDateEditText.setOnClickListener {
            showDatePicker(endDateEditText)
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add Task")
            .setPositiveButton("Add") { _, _ ->
                val taskName = dialogView.findViewById<EditText>(R.id.taskNameEditText).text.toString()
                val priority = prioritySpinner.selectedItemPosition + 1
                val endDate = endDateEditText.text.toString()
                addTask(taskName, priority, endDate)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_task, null)
        val prioritySpinner = dialogView.findViewById<Spinner>(R.id.prioritySpinner)
        setupPrioritySpinner(prioritySpinner)

        val endDateEditText = dialogView.findViewById<EditText>(R.id.endDateEditText)
        endDateEditText.setText(task.endDate)
        endDateEditText.setOnClickListener {
            showDatePicker(endDateEditText)
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Edit Task")
            .setPositiveButton("Save") { _, _ ->
                val newTaskName = dialogView.findViewById<EditText>(R.id.taskNameEditText).text.toString()
                val newPriority = prioritySpinner.selectedItemPosition + 1
                val newEndDate = endDateEditText.text.toString()
                editTask(task.copy(name = newTaskName, priority = newPriority, endDate = newEndDate))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addTask(name: String, priority: Int, endDate: String) {
        val newTask = Task(name = name, categoryId = categoryId, priority = priority, endDate = endDate)
        db.collection("tasks").add(newTask)
            .addOnSuccessListener { documentReference ->
                newTask.id = documentReference.id
                tasks.add(newTask)
                taskAdapter.notifyDataSetChanged()

                val date = dateFormatter.parse(endDate) ?: return@addOnSuccessListener
                scheduleTaskReminder(this,name, endDate, date.time)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }




    private fun editTask(task: Task) {
        db.collection("tasks").document(task.id).set(task)
            .addOnSuccessListener {
                fetchTasks()
            }
    }

    private fun deleteTask(task: Task) {
        db.collection("tasks").document(task.id)
            .delete()
            .addOnSuccessListener {
                tasks.remove(task)
                taskAdapter.notifyDataSetChanged()
            }
    }

    private fun completeTask(task: Task) {
        val updatedTask = task.copy(completed = true)
        db.collection("tasks").document(task.id).update("completed", true)
            .addOnSuccessListener {
                val index = tasks.indexOfFirst { it.id == task.id }
                if (index != -1) {
                    tasks[index] = updatedTask
                    taskAdapter.notifyItemChanged(index)
                } else {
                    // Fallback in case task is not found
                    fetchTasks()
                }
            }
    }


    private fun searchTasks(query: String) {
        val filteredTasks = if (query.isEmpty()) {
            tasks
        } else {
            tasks.filter { it.name.contains(query, ignoreCase = true) }
        }
        taskAdapter = TaskAdapter(filteredTasks, onEditClick = { task -> showEditTaskDialog(task) },
            onDeleteClick = { task -> deleteTask(task) }, onCompleteClick = { task -> completeTask(task) })
        taskRecyclerView.adapter = taskAdapter
    }

    private fun setupPrioritySpinner(spinner: Spinner) {
        val priorities = listOf("Low", "Medium", "High")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                editText.setText(dateFormatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
