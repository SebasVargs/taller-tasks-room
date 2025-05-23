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
    @Query("select * from priority order by name asc")
    fun all(): Flow<List<Priority>>

    @Query("select * from priority where id=:id")
    fun findByID(id: Int): Flow<Priority>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(vararg newPriority: Priority)

    @Update
    fun update(vararg priority: Priority)

    @Delete
    fun delete(vararg priority: Priority)
}