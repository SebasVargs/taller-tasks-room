package com.example.taller_moviles.database.repository

import com.example.taller_moviles.database.dao.PriorityDao
import com.example.taller_moviles.database.entities.Priority
import kotlinx.coroutines.flow.Flow

class PriorityRepository(private val dao: PriorityDao) {

    fun getPriorities(): Flow<List<Priority>> {
        return this.dao.getAllPriorities()
    }

    suspend fun findPriorityById(id: Int): Priority? {
        return dao.getPriorityById(id)
    }

    suspend fun saveNewPriority(priority: Priority) {
        dao.insertPriority(priority)
    }

}