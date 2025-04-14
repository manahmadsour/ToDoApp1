package com.example.todoapp

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class TaskReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val taskName = inputData.getString("taskName") ?: "Task"
        val taskEndDate = inputData.getString("taskEndDate") ?: "Due Soon"
        NotificationUtil.showNotification(applicationContext, "Reminder: $taskName", "Due Date: $taskEndDate")
        return Result.success()
    }
}
