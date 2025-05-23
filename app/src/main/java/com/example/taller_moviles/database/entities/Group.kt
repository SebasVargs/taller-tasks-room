package com.example.taller_moviles.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "group")
data class Group(
    @PrimaryKey val id: Int,
    @ColumnInfo val name: String
)