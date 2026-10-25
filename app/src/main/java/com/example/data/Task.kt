package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,
    val dueTime: Long? = null, // Epoch millis
    val isReminderEnabled: Boolean = false,
    val reminderDuration: Int = 30, // seconds
    val reminderSound: String = "System Default",
    val createdAt: Long = System.currentTimeMillis(),
    val firestoreId: String? = null
)

enum class Priority {
    HIGH, MEDIUM, LOW
}
