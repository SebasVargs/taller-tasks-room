package com.example.taller_moviles.database.entities

import android.net.http.UrlRequest.Status
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.taller_moviles.database.converters.DateConverters
import java.time.LocalDateTime

@Entity(
    tableName = "task",
    foreignKeys = [
        ForeignKey(
            entity = Priority::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("id_priority"),
            onDelete = ForeignKey.CASCADE

        ),
        ForeignKey(
            entity = Group::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("id_group"),
            onDelete = ForeignKey.CASCADE

        )
    ],
    indices = [
        Index(value = ["id_priority"]),
        Index(value = ["id_group"])
    ]
)

@TypeConverters(DateConverters::class)
data class Task (
    @PrimaryKey val id: Int,
    @ColumnInfo val description: String,
    @ColumnInfo(name = "create_date") val create_date: LocalDateTime ,
    @ColumnInfo(name = "limit_time") val limit_time: LocalDateTime ,
    @ColumnInfo(name = "finish_date") val finish_date: LocalDateTime? ,
    @ColumnInfo(name = "status") val status: Boolean,
    @ColumnInfo(name= "id_priority") val id_priority: Int,
    @ColumnInfo(name = "id_group") val id_group: Int
)