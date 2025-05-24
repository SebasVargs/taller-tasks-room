package com.example.taller_moviles.database.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.taller_moviles.database.connection.AppDatabase
import com.example.taller_moviles.database.entities.Group
import com.example.taller_moviles.database.entities.Priority
import com.example.taller_moviles.database.entities.Task
import com.example.taller_moviles.database.models.TaskWithDetails

@RequiresApi(Build.VERSION_CODES.O)
class TaskViewModel(private val context: Context) : ViewModel() {

    private val database = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "task_database"
    ).build()

    private val taskDao = database.taskDao()
    private val priorityDao = database.priorityDao()
    private val groupDao = database.groupDao()

    val tasks = taskDao.getTasksWithDetails()
    val priorities = priorityDao.getAllPriorities()
    val groups = groupDao.getAllGroups()

    private val _overdueTasks = MutableStateFlow<List<TaskWithDetails>>(emptyList())
    val overdueTasks: StateFlow<List<TaskWithDetails>> = _overdueTasks.asStateFlow()

    private val _searchResults = MutableStateFlow<List<TaskWithDetails>>(emptyList())
    val searchResults: StateFlow<List<TaskWithDetails>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        initializeDatabase()
        checkOverdueTasks()
    }

    private fun initializeDatabase() {
        viewModelScope.launch {
            val priorityCount = priorityDao.getPriorityCount()
            if (priorityCount == 0) {
                val defaultPriorities = listOf(
                    Priority(1, "Urgente"),
                    Priority(2, "Importante"),
                    Priority(3, "Toca hacerla"),
                    Priority(4, "No tan importante")
                )
                defaultPriorities.forEach { priorityDao.insertPriority(it) }

                // Agregar algunos grupos por defecto
                val defaultGroups = listOf(
                    Group(1, "Trabajo"),
                    Group(2, "Personal"),
                    Group(3, "Estudios")
                )
                defaultGroups.forEach { groupDao.insertGroup(it) }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addTask(description: String, limitTime: LocalDateTime, priorityId: Int, groupId: Int) {
        viewModelScope.launch {
            val task = Task(
                id = System.currentTimeMillis().toInt(),
                description = description,
                create_date = LocalDateTime.now(),
                limit_time = limitTime,
                finish_date = null,
                status = false,
                id_priority = priorityId,
                id_group = groupId
            )
            taskDao.insertTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun finishTask(taskId: Int) {
        viewModelScope.launch {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            taskDao.finishTask(taskId, LocalDateTime.now().format(formatter))
        }
    }

    fun searchTasks(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _isSearching.value = false
                _searchResults.value = emptyList()
            } else {
                _isSearching.value = true
                taskDao.searchTasks("%$query%").collect {
                    _searchResults.value = it
                }
            }
        }
    }

    fun clearSearch() {
        _isSearching.value = false
        _searchResults.value = emptyList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkOverdueTasks() {
        viewModelScope.launch {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val currentDate = LocalDateTime.now().format(formatter)
            taskDao.getOverdueTasks(currentDate).collect {
                _overdueTasks.value = it
            }
        }
    }

    fun addGroup(name: String) {
        viewModelScope.launch {
            val group = Group(
                id = System.currentTimeMillis().toInt(),
                name = name
            )
            groupDao.insertGroup(group)
        }
    }

    fun updateGroup(group: Group) {
        viewModelScope.launch {
            groupDao.updateGroup(group)
        }
    }

    fun deleteGroup(group: Group) {
        viewModelScope.launch {
            groupDao.deleteGroup(group)
        }
    }
}