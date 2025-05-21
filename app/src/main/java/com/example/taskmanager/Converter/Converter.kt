package com.example.taskmanager.Converter

import androidx.room.TypeConverter
import com.example.taskmanager.Entities.Task
import java.util.Date

class Converter {
@TypeConverter
fun FromDateTask(value: Long): Date?{
return value?.let { Date(it) }
}
    @TypeConverter
    fun DateConvertion(date: Date?): Long?{
        return date?.time?.toLong()
    }
}
