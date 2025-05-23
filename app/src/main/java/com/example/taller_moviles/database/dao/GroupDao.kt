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
    @Query("select * from `group` order by name asc")
    fun all(): Flow<List<Group>>

    @Query("select * from `group` where id=:id")
    fun findByID(id: Int): Flow<Group>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(vararg newGroup: Group)

    @Update
    fun update(vararg group: Group)

    @Delete
    fun delete(vararg group: Group)
}