package com.example.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.FirestoreTaskRepository
import com.example.data.Priority
import com.example.data.Task
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: FirestoreTaskRepository) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.getAllTasks()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val completionPercentage: StateFlow<Float> = tasks.map { list ->
        if (list.isEmpty()) 0f
        else {
            val completed = list.count { it.isCompleted }
            completed.toFloat() / list.size
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    fun addTask(
        title: String, 
        description: String, 
        priority: Priority, 
        dueTime: Long?,
        isReminderEnabled: Boolean = false,
        reminderDuration: Int = 30,
        reminderSound: String = "System Default"
    ) {
        viewModelScope.launch {
            repository.insert(
                Task(
                    title = title, 
                    description = description, 
                    priority = priority, 
                    dueTime = dueTime,
                    isReminderEnabled = isReminderEnabled,
                    reminderDuration = reminderDuration,
                    reminderSound = reminderSound
                )
            )
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.update(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }
}

class TaskViewModelFactory(private val repository: FirestoreTaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
