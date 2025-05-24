package com.example.taller_moviles.database.models

import java.time.LocalDateTime

data class TaskWithDetails(
    val id: Int,
    val description: String,
    val create_date: LocalDateTime,
    val limit_time: LocalDateTime,
    val finish_date: LocalDateTime?,
    val status: Boolean,
    val id_priority: Int,
    val id_group: Int,
    val priority_name: String,
    val group_name: String
)