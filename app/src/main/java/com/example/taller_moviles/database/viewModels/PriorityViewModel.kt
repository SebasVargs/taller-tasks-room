package com.example.taller_moviles.database.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller_moviles.database.connection.AppDatabase
import com.example.taller_moviles.database.connection.ConexDb
import com.example.taller_moviles.database.entities.Group
import com.example.taller_moviles.database.entities.Priority
import com.example.taller_moviles.database.repository.GroupRepository
import com.example.taller_moviles.database.repository.PriorityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PriorityViewModel(context: Context): ViewModel() {

    private val priorityRepository: PriorityRepository

    init {
        val conex: AppDatabase = ConexDb.getDatabase(context)
        priorityRepository = PriorityRepository(dao = conex.priorityDao())
    }

    fun queryPriorities(success: (data: List<Priority>) -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            priorityRepository.getPriorities().collect {
                success(it)
            }
        }
    }

    fun findById(
        id: Int,
        success: (data: Priority?) -> Unit // ← admite null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = priorityRepository.findPriorityById(id)
            success(result)
        }
    }

    fun savePriority(data: Priority) {
        viewModelScope.launch(Dispatchers.IO) {
            priorityRepository.saveNewPriority(data)
        }
    }

}
