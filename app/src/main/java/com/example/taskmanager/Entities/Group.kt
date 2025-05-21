package com.example.taskmanager.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Group")
data class Group(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
