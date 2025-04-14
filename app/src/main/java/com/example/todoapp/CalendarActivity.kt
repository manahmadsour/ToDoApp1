package com.example.todoapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var uid: String
    private val db = FirebaseFirestore.getInstance()
    private val tasksList = mutableListOf<Task>()
    private val categoryIds = mutableListOf<String>()
    private val taskDates = mutableSetOf<String>()  // To track dates that have tasks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        calendarView = findViewById(R.id.calendarView)
        uid = intent.getStringExtra("uid") ?: ""

        if (uid.isNotEmpty()) {
            loadCategories(uid)
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$year-${month + 1}-$dayOfMonth" // Month is 0-based
            val taskForSelectedDate = tasksList.filter { it.endDate == selectedDate }

            if (taskForSelectedDate.isNotEmpty()) {
                showTaskDetailsDialog(taskForSelectedDate)
            } else {
                showAddTaskDialog(selectedDate)
            }
        }
    }

    private fun loadCategories(userId: String) {
        db.collection("categories")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                categoryIds.clear()
                for (document in documents) {
                    val categoryId = document.getString("id") ?: document.id
                    categoryIds.add(categoryId)
                }

                if (categoryIds.isNotEmpty()) {
                    loadTasks(categoryIds)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load categories: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadTasks(categoryIds: List<String>) {
        db.collection("tasks")
            .whereIn("categoryId", categoryIds)
            .get()
            .addOnSuccessListener { documents ->
                tasksList.clear()
                taskDates.clear()  // Clear old task dates
                for (document in documents) {
                    val task = document.toObject(Task::class.java)
                    tasksList.add(task)
                    taskDates.add(task.endDate)  // Add task date to the set
                }
                updateCalendarHighlight()  // Update the calendar after loading tasks
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load tasks: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showTaskDetailsDialog(tasks: List<Task>) {
        val task = tasks.first()

        val builder = AlertDialog.Builder(this)
        builder.setTitle(task.name)
        builder.setMessage("Task: ${task.name}\nPriority: ${task.priority}\nCompleted: ${task.completed}\nEnd Date: ${task.endDate}")
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun showAddTaskDialog(date: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_taskcalendar, null)
        val taskNameEditText = dialogView.findViewById<EditText>(R.id.NameEditText)
        val prioritySpinner = dialogView.findViewById<Spinner>(R.id.PriorityS)

        val priorities = listOf("Low", "Medium", "High")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        prioritySpinner.adapter = adapter

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add Task on $date")
            .setPositiveButton("Add") { _, _ ->
                val taskName = taskNameEditText.text.toString()
                val priority = prioritySpinner.selectedItemPosition + 1

                // Use the first category for simplicity
                if (categoryIds.isNotEmpty()) {
                    saveTask(taskName, priority, date, categoryIds.first())
                } else {
                    Toast.makeText(this, "No categories available", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveTask(name: String, priority: Int, endDate: String, categoryId: String) {
        val newTask = Task(name = name, priority = priority, endDate = endDate, categoryId = categoryId)

        db.collection("tasks").add(newTask)
            .addOnSuccessListener {
                Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show()
                tasksList.add(newTask)  // Add the new task to the local list
                taskDates.add(endDate)  // Add the task date to the set
                updateCalendarHighlight()  // Highlight the date in the calendar
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add task", Toast.LENGTH_SHORT).show()
            }
    }

    // Method to highlight days with tasks in the calendar
    private fun updateCalendarHighlight() {
        calendarView.setDate(calendarView.date, false, false)  // Reset calendar view

        taskDates.forEach { date ->
            // Convert date format to match with CalendarView date format
            val parts = date.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1  // Calendar month is 0-based
            val dayOfMonth = parts[2].toInt()

            // Set the date for each task to be highlighted
            val dateMillis = getCalendarMillis(year, month, dayOfMonth)
            calendarView.setDate(dateMillis, true, true)
        }
    }

    // Helper function to convert date to milliseconds
    private fun getCalendarMillis(year: Int, month: Int, dayOfMonth: Int): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        return calendar.timeInMillis
    }
}
