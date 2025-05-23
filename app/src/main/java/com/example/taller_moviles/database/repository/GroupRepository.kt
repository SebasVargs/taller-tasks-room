package com.example.taller_moviles.database.repository

import com.example.taller_moviles.database.dao.GroupDao
import com.example.taller_moviles.database.entities.Group
import kotlinx.coroutines.flow.Flow

class GroupRepository(private val dao: GroupDao){

    fun getGroups(): Flow<List<Group>>{
        return this.dao.all()
    }

    fun findGroupById(id: Int): Flow<Group>{
        return this.dao.findByID(id)
    }

    fun saveNewGroup(group: Group){
        this.dao.save(group)
    }

    fun updateGroup(group: Group){
        this.dao.update(group)
    }

    fun deleteGroup(group: Group){
        this.dao.delete(group)
    }
}