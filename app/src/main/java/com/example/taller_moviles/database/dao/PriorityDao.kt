package com.example.taller_moviles.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.taller_moviles.database.entities.Priority
import kotlinx.coroutines.flow.Flow

@Dao
interface PriorityDao {
    @Query("SELECT * FROM priority")
    fun getAllPriorities(): Flow<List<Priority>>

    @Query("SELECT * FROM priority WHERE id = :id")
    suspend fun getPriorityById(id: Int): Priority?

    @Insert
    suspend fun insertPriority(priority: Priority)

    @Query("SELECT COUNT(*) FROM priority")
    suspend fun getPriorityCount(): Int
}