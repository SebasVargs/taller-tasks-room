package com.example.taller_moviles.database.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller_moviles.database.connection.AppDatabase
import com.example.taller_moviles.database.connection.ConexDb
import com.example.taller_moviles.database.entities.Group
import com.example.taller_moviles.database.repository.GroupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GroupViewModel(context: Context): ViewModel() {

    private val groupRepository: GroupRepository

    init {
        val conex: AppDatabase = ConexDb.getDatabase(context)
        groupRepository = GroupRepository(dao = conex.groupDao())
    }

    fun queryGroups(success: (data: List<Group>) -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            groupRepository.getGroups().collect {
                success(it)
            }
        }
    }

    fun findById(
        id: Int,
        success: (data: Group) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            groupRepository.findGroupById(id).collect {
                success(it)
            }
        }
    }

    fun saveGroup(data: Group) {
        viewModelScope.launch(Dispatchers.IO) {
            groupRepository.saveNewGroup(data)
        }
    }

    fun deleteGroup(data: Group) {
        viewModelScope.launch(Dispatchers.IO) {
            groupRepository.deleteGroup(data)
        }
    }

}