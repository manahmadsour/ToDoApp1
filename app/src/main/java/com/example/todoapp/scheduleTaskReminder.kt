package com.example.todoapp

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

fun scheduleTaskReminder(context: Context, taskName: String, taskEndDate: String, remindAtMillis: Long) {
    val delay = remindAtMillis - System.currentTimeMillis()
    if (delay > 0) {
        val data = Data.Builder()
            .putString("taskName", taskName)
            .putString("taskEndDate", taskEndDate)
            .build()

        val reminderRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(reminderRequest)
    }
}
