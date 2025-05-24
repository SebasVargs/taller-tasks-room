package com.example.taller_moviles.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.taller_moviles.database.entities.Group
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM task_group")
    fun getAllGroups(): Flow<List<Group>>

    @Insert
    suspend fun insertGroup(group: Group)

    @Update
    suspend fun updateGroup(group: Group)

    @Delete
    suspend fun deleteGroup(group: Group)

    @Query("SELECT * FROM task_group WHERE id = :id")
    suspend fun getGroupById(id: Int): Group?
}