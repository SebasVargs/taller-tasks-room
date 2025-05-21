package com.example.taskmanager.Entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.taskmanager.Converter.Converter
import java.time.LocalDateTime

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Priority::class,
            parentColumns = ["id"],
            childColumns = ["id_priority"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Group::class,
            parentColumns = ["id"],
            childColumns = ["id_group"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(Converter::class)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val createdDate: LocalDateTime,
    val limitDate: LocalDateTime,
    val finishDate: LocalDateTime,
    val isCompleted: Boolean = false,
    val id_priority: Int,
    val id_group: Int
)
