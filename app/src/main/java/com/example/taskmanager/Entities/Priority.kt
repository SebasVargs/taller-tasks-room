package com.example.taskmanager.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Priority")
data class Priority(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
