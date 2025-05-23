package com.example.taller_moviles.database.connection

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.taller_moviles.database.converters.DateConverters
import com.example.taller_moviles.database.dao.GroupDao
import com.example.taller_moviles.database.dao.PriorityDao
import com.example.taller_moviles.database.dao.TaskDao
import com.example.taller_moviles.database.entities.Group
import com.example.taller_moviles.database.entities.Priority
import com.example.taller_moviles.database.entities.Task


@Database(entities = [Task::class, Priority::class, Group::class], version = 1)
@TypeConverters(DateConverters::class)  // Registra el conversor de fechas
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun priorityDao(): PriorityDao
    abstract fun groupDao(): GroupDao
}