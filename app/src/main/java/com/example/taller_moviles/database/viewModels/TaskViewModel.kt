package com.example.taller_moviles.database.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller_moviles.database.connection.AppDatabase
import com.example.taller_moviles.database.connection.ConexDb
import com.example.taller_moviles.database.entities.Task
import com.example.taller_moviles.database.repository.TaskRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

class TaskViewModel(context: Context): ViewModel() {

    private val taskRepository: TaskRepository

    init {
        val conex: AppDatabase = ConexDb.getDatabase(context)
        taskRepository = TaskRepository(dao = conex.taskDao())
    }

    fun queryTasks(success: (data: List<Task>) -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            taskRepository.getTasks().collect {
                success(it)
            }
        }
    }

    fun findById(
        id: Int,
        success: (data:Task) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            taskRepository.findTaskById(id).collect {
                success(it)
            }
        }
    }

    fun saveTask(data: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.saveNewTask(data)
        }
    }

    fun deleteTask(data: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.deleteTask(data)
        }
    }

}