package com.example.taller_moviles.database.repository

import com.example.taller_moviles.database.dao.PriorityDao
import com.example.taller_moviles.database.entities.Group
import com.example.taller_moviles.database.entities.Priority
import kotlinx.coroutines.flow.Flow

class PriorityRepository(private val dao: PriorityDao) {

    fun getPriorities(): Flow<List<Priority>> {
        return this.dao.all()
    }

    fun findPriorityById(id: Int): Flow<Priority> {
        return this.dao.findByID(id)
    }

    fun saveNewPriority(priority: Priority){
        this.dao.save(priority)
    }

    fun updatePriority(priority: Priority){
        this.dao.update(priority)
    }

    fun deletePriority(priority: Priority){
        this.dao.delete(priority)
    }
}