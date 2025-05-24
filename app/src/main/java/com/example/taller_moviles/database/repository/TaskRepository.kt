package com.example.taller_moviles.database.repository

import com.example.taller_moviles.database.dao.TaskDao
import com.example.taller_moviles.database.entities.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    fun getTasks(): Flow<List<Task>> {
        return this.dao.getAllTasks()
    }

    suspend fun findTaskById(id: Int): Task? {
        return dao.getTaskById(id)
    }

    suspend fun saveNewTask(task: Task) {
        dao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        dao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        dao.deleteTask(task)
    }

}