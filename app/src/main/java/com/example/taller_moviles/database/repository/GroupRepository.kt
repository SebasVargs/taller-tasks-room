package com.example.taller_moviles.database.repository

import com.example.taller_moviles.database.dao.GroupDao
import com.example.taller_moviles.database.entities.Group
import kotlinx.coroutines.flow.Flow

class GroupRepository(private val dao: GroupDao){

    fun getGroups(): Flow<List<Group>>{
        return this.dao.getAllGroups()
    }

    suspend fun findGroupById(id: Int): Group? {
        return dao.getGroupById(id)
    }

    suspend fun saveNewGroup(group: Group) {
        dao.insertGroup(group)
    }

    suspend fun updateGroup(group: Group) {
        dao.updateGroup(group)
    }

    suspend fun deleteGroup(group: Group) {
        dao.deleteGroup(group)
    }

}