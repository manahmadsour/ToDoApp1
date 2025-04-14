package com.example.todoapp

data class Task (
    var id: String ="",
    val categoryId: String = "",
    val priority: Int = 0,
    val name: String ="",
    val completed: Boolean = false,
    val endDate: String = "",
    val taskColor: String = "",
    val taskTextColor: String = ""
)
