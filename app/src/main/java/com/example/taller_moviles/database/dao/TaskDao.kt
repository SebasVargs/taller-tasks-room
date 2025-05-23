package com.example.taller_moviles.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.taller_moviles.database.entities.Task
import kotlinx.coroutines.flow.Flow

interface TaskDao {
    @Query("select * from task order by id asc")
    fun all(): Flow<List<Task>>

    @Query("select * from task where id=:id")
    fun findByID(id: Int): Flow<Task>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(vararg newTask: Task)

    @Update
    fun update(vararg task: Task)

    @Delete
    fun delete(vararg task: Task)
}