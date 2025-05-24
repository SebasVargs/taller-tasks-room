package com.example.taller_moviles.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.taller_moviles.database.entities.Task
import com.example.taller_moviles.database.models.TaskWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM task")
    fun getAllTasks(): Flow<List<Task>>

    @Query("""
        SELECT t.*, p.name as priority_name, g.name as group_name 
        FROM task t 
        INNER JOIN priority p ON t.id_priority = p.id 
        INNER JOIN task_group g ON t.id_group = g.id 
        ORDER BY g.name, t.limit_time ASC
    """)
    fun getTasksWithDetails(): Flow<List<TaskWithDetails>>

    @Query("""
        SELECT t.*, p.name as priority_name, g.name as group_name 
        FROM task t 
        INNER JOIN priority p ON t.id_priority = p.id 
        INNER JOIN task_group g ON t.id_group = g.id 
        WHERE t.status = 0 AND t.limit_time < :currentDate
    """)
    fun getOverdueTasks(currentDate: String): Flow<List<TaskWithDetails>>

    @Query("""
        SELECT t.*, p.name as priority_name, g.name as group_name 
        FROM task t 
        INNER JOIN priority p ON t.id_priority = p.id 
        INNER JOIN task_group g ON t.id_group = g.id 
        WHERE t.description LIKE :searchQuery 
        OR p.name LIKE :searchQuery 
        OR g.name LIKE :searchQuery
        ORDER BY g.name, t.limit_time ASC
    """)
    fun searchTasks(searchQuery: String): Flow<List<TaskWithDetails>>

    @Insert
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("UPDATE task SET status = 1, finish_date = :finishDate WHERE id = :taskId")
    suspend fun finishTask(taskId: Int, finishDate: String)

    @Query("SELECT * FROM task WHERE id = :id")
    suspend fun getTaskById(id: Int): Task?
}