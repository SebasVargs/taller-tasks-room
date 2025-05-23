package com.example.taller_moviles.database.repository

import com.example.taller_moviles.database.dao.TaskDao
import com.example.taller_moviles.database.entities.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    fun getTasks(): Flow<List<Task>> {
        return this.dao.all()
    }

    fun findTaskById(id:Int) : Flow<Task> {
        return this.dao.findByID(id)
    }

    fun saveNewTask(task: Task){
        this.dao.save(task)
    }

    fun updateTask(task: Task){
        this.dao.update(task)
    }

    fun deleteTask(task: Task){
        this.dao.delete(task)
    }

}