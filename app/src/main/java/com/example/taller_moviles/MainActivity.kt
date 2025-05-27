package com.example.taller_moviles

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taller_moviles.database.entities.Task
import com.example.taller_moviles.database.models.TaskWithDetails
import com.example.taller_moviles.database.viewModels.TaskViewModel
import com.example.taller_moviles.ui.theme.Taller_MovilesTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Taller_MovilesTheme {
                AppNavigation()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController)
        }
        composable("group") {
            GroupScreen(navController)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel = remember { TaskViewModel(context) }

    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    val overdueTasks by viewModel.overdueTasks.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    var showDeleteTaskDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<TaskWithDetails?>(null) }
    var taskToEdit by remember { mutableStateOf<TaskWithDetails?>(null) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Tareas") },
                actions = {
                    IconButton(onClick = { showAddTaskDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Tarea")
                    }
                    IconButton(onClick = { navController.navigate("group") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Gestionar Grupos")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Buscador de tareas
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (it.isBlank()) {
                        viewModel.clearSearch()
                    } else {
                        viewModel.searchTasks(it)
                    }
                },
                label = { Text("Buscar tareas...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.clearSearch()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Alertas de tareas vencidas
            if (overdueTasks.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "⚠️ Tareas Vencidas (${overdueTasks.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        overdueTasks.take(3).forEach { task ->
                            Text(
                                text = "• ${task.description}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Red.copy(alpha = 0.8f)
                            )
                        }
                        if (overdueTasks.size > 3) {
                            Text(
                                text = "... y ${overdueTasks.size - 3} más",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Red.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Lista de tareas agrupadas y ordenadas por prioridad
            val displayTasks = if (isSearching) searchResults else tasks
            val groupedTasks = displayTasks.groupBy { it.group_name }

            LazyColumn {
                groupedTasks.forEach { (groupName, groupTasks) ->
                    item {
                        Text(
                            text = groupName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Ordenar por prioridad (id_priority ascendente = mayor prioridad primero)
                    // y luego por fecha límite
                    items(groupTasks.sortedWith(compareBy<TaskWithDetails> { it.id_priority }.thenBy { it.limit_time })) { task ->
                        TaskItem(
                            task = task,
                            onFinish = { viewModel.finishTask(it) },
                            onReopen = { viewModel.reopenTask(it) },
                            onDelete = {
                                taskToDelete = task
                                showDeleteTaskDialog = true
                            },
                            onClick = { taskToEdit = task }
                        )
                    }
                }
            }
        }
    }

    // Modal para agregar nueva tarea
    if (showAddTaskDialog) {
        AddTaskDialog(
            viewModel = viewModel,
            onDismiss = { showAddTaskDialog = false }
        )
    }

    if (showDeleteTaskDialog && taskToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteTaskDialog = false
                taskToDelete = null
            },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar la tarea \"${taskToDelete!!.description}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val taskEntity = Task(
                            taskToDelete!!.id, taskToDelete!!.description, taskToDelete!!.create_date,
                            taskToDelete!!.limit_time, taskToDelete!!.finish_date, taskToDelete!!.status,
                            taskToDelete!!.id_priority, taskToDelete!!.id_group
                        )
                        viewModel.deleteTask(taskEntity)
                        showDeleteTaskDialog = false
                        taskToDelete = null
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteTaskDialog = false
                        taskToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de edición de tarea
    if (taskToEdit != null) {
        TaskEditDialog(
            task = taskToEdit!!,
            viewModel = viewModel,
            onDismiss = { taskToEdit = null }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    viewModel: TaskViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var description by remember { mutableStateOf("") }
    var selectedPriorityId by remember { mutableIntStateOf(1) }
    var selectedGroupId by remember { mutableIntStateOf(1) }
    var limitDate by remember { mutableStateOf("") }
    var limitTime by remember { mutableStateOf("") }

    val priorities by viewModel.priorities.collectAsState(initial = emptyList())
    val groups by viewModel.groups.collectAsState(initial = emptyList())

    // Función para validar y crear tarea
    fun validateAndCreateTask() {
        when {
            description.isBlank() -> {
                Toast.makeText(context, "La descripción es obligatoria", Toast.LENGTH_SHORT).show()
            }
            limitDate.isBlank() -> {
                Toast.makeText(context, "La fecha límite es obligatoria", Toast.LENGTH_SHORT).show()
            }
            limitTime.isBlank() -> {
                Toast.makeText(context, "La hora límite es obligatoria", Toast.LENGTH_SHORT).show()
            }
            !isValidDateFormat(limitDate) -> {
                Toast.makeText(context, "Formato de fecha inválido. Use YYYY-MM-DD", Toast.LENGTH_LONG).show()
            }
            !isValidTimeFormat(limitTime) -> {
                Toast.makeText(context, "Formato de hora inválido. Use HH:MM", Toast.LENGTH_LONG).show()
            }
            !isValidDate(limitDate) -> {
                Toast.makeText(context, "La fecha ingresada no es válida", Toast.LENGTH_SHORT).show()
            }
            !isValidTime(limitTime) -> {
                Toast.makeText(context, "La hora ingresada no es válida", Toast.LENGTH_SHORT).show()
            }
            isPastDateTime(limitDate, limitTime) -> {
                Toast.makeText(context, "La fecha y hora límite no puede ser en el pasado", Toast.LENGTH_LONG).show()
            }
            priorities.find { it.id == selectedPriorityId } == null -> {
                Toast.makeText(context, "Debe seleccionar una prioridad válida", Toast.LENGTH_SHORT).show()
            }
            groups.find { it.id == selectedGroupId } == null -> {
                Toast.makeText(context, "Debe seleccionar un grupo válido", Toast.LENGTH_SHORT).show()
            }
            else -> {
                try {
                    val limitDateTime = LocalDateTime.parse("${limitDate}T${limitTime}:00")
                    viewModel.addTask(description, limitDateTime, selectedPriorityId, selectedGroupId)

                    Toast.makeText(context, "Tarea creada exitosamente", Toast.LENGTH_SHORT).show()
                    onDismiss()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al crear la tarea: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Tarea") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = description.isBlank() && description != "",
                    supportingText = if (description.isBlank() && description != "") {
                        { Text("Campo obligatorio", color = MaterialTheme.colorScheme.error) }
                    } else null
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = limitDate,
                        onValueChange = { limitDate = it },
                        label = { Text("Fecha Limite * (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("2024-12-31") },
                        isError = limitDate.isNotBlank() && !isValidDateFormat(limitDate)
                    )

                    OutlinedTextField(
                        value = limitTime,
                        onValueChange = { limitTime = it },
                        label = { Text("Hora Limite * (HH:MM)") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("23:59") },
                        isError = limitTime.isNotBlank() && !isValidTimeFormat(limitTime)
                    )
                }

                // Dropdown para prioridad
                var expandedPriority by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedPriority,
                    onExpandedChange = { expandedPriority = !expandedPriority }
                ) {
                    OutlinedTextField(
                        value = priorities.find { it.id == selectedPriorityId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Prioridad *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPriority,
                        onDismissRequest = { expandedPriority = false }
                    ) {
                        priorities.forEach { priority ->
                            DropdownMenuItem(
                                text = { Text(priority.name) },
                                onClick = {
                                    selectedPriorityId = priority.id
                                    expandedPriority = false
                                }
                            )
                        }
                    }
                }

                // Dropdown para grupo
                var expandedGroup by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedGroup,
                    onExpandedChange = { expandedGroup = !expandedGroup }
                ) {
                    OutlinedTextField(
                        value = groups.find { it.id == selectedGroupId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grupo *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGroup) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGroup,
                        onDismissRequest = { expandedGroup = false }
                    ) {
                        groups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = {
                                    selectedGroupId = group.id
                                    expandedGroup = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { validateAndCreateTask() }
            ) {
                Text("Crear Tarea")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Funciones de validación
@RequiresApi(Build.VERSION_CODES.O)
fun isValidDateFormat(date: String): Boolean {
    return date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
}

fun isValidTimeFormat(time: String): Boolean {
    return time.matches(Regex("\\d{2}:\\d{2}"))
}

@RequiresApi(Build.VERSION_CODES.O)
fun isValidDate(date: String): Boolean {
    return try {
        if (!isValidDateFormat(date)) return false
        val parts = date.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()

        when {
            year < 1900 || year > 2100 -> false
            month < 1 || month > 12 -> false
            day < 1 || day > 31 -> false
            month == 2 && day > 29 -> false
            month == 2 && day == 29 && !LocalDate.of(year, month, day).isLeapYear -> false
            (month == 4 || month == 6 || month == 9 || month == 11) && day > 30 -> false
            else -> {
                LocalDate.parse(date)
                true
            }
        }
    } catch (e: Exception) {
        false
    }
}

fun isValidTime(time: String): Boolean {
    return try {
        if (!isValidTimeFormat(time)) return false
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        hour in 0..23 && minute in 0..59
    } catch (e: Exception) {
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun isPastDateTime(date: String, time: String): Boolean {
    return try {
        val dateTime = LocalDateTime.parse("${date}T${time}:00")
        dateTime.isBefore(LocalDateTime.now())
    } catch (e: Exception) {
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel = remember { TaskViewModel(context) }

    var groupName by remember { mutableStateOf("") }
    val groups by viewModel.groups.collectAsState(initial = emptyList())

    var showDeleteGroupDialog by remember { mutableStateOf(false) }
    var groupToDelete by remember { mutableStateOf<com.example.taller_moviles.database.entities.Group?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Grupos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Formulario para agregar grupo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Nuevo Grupo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Nombre del Grupo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (groupName.isNotBlank()) {
                                viewModel.addGroup(groupName)
                                groupName = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Agregar Grupo")
                    }
                }
            }

            // Lista de grupos
            Text(
                text = "Grupos Existentes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn {
                items(groups) { group ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            IconButton(
                                onClick = {
                                    groupToDelete = group
                                    showDeleteGroupDialog = true
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar Grupo",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar grupo
    if (showDeleteGroupDialog && groupToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteGroupDialog = false
                groupToDelete = null
            },
            title = { Text("Confirmar eliminación") },
            text = {
                Text("¿Estás seguro de que deseas eliminar el grupo \"${groupToDelete!!.name}\"?\n\nEsto también eliminará todas las tareas asociadas a este grupo.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGroup(groupToDelete!!)
                        showDeleteGroupDialog = false
                        groupToDelete = null
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteGroupDialog = false
                        groupToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskItem(
    task: TaskWithDetails,
    onFinish: (Int) -> Unit,
    onReopen: (Int) -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val tealColor = colorResource(R.color.teal_700)
    val makeColor = colorResource(R.color.teal_200)
    val isOverdue = !task.status && task.limit_time.isBefore(LocalDateTime.now())
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                task.status -> Color.Green.copy(alpha = 0.1f)
                isOverdue -> Color.Red.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Prioridad: ${task.priority_name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = when (task.priority_name) {
                            "Urgente" -> Color.Red
                            "Importante" -> tealColor
                            "Toca hacerla" -> makeColor
                            else -> Color.Gray
                        }
                    )

                    Text(
                        text = "Vence: ${task.limit_time.format(formatter)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) Color.Red else Color.Gray
                    )

                    if (task.status) {
                        Text(
                            text = "✅ Completada: ${task.finish_date?.format(formatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Green
                        )
                    }
                }

                Row {
                    if (!task.status) {
                        IconButton(onClick = { onFinish(task.id) }) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Finalizar",
                                tint = Color.Green
                            )
                        }
                    } else {
                        IconButton(onClick = { onReopen(task.id) }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Reabrir",
                                tint = Color.Blue
                            )
                        }
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditDialog(
    task: TaskWithDetails,
    viewModel: TaskViewModel,
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf(task.description) }
    var selectedPriorityId by remember { mutableIntStateOf(task.id_priority) }
    var selectedGroupId by remember { mutableIntStateOf(task.id_group) }

    // Formatear fecha y hora para los campos
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    var limitDate by remember { mutableStateOf(task.limit_time.format(formatter)) }
    var limitTime by remember { mutableStateOf(task.limit_time.format(timeFormatter)) }

    val priorities by viewModel.priorities.collectAsState(initial = emptyList())
    val groups by viewModel.groups.collectAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Tarea") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = limitDate,
                        onValueChange = { limitDate = it },
                        label = { Text("Fecha") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = limitTime,
                        onValueChange = { limitTime = it },
                        label = { Text("Hora") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Dropdown para prioridad
                var expandedPriority by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedPriority,
                    onExpandedChange = { expandedPriority = !expandedPriority }
                ) {
                    OutlinedTextField(
                        value = priorities.find { it.id == selectedPriorityId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Prioridad") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPriority,
                        onDismissRequest = { expandedPriority = false }
                    ) {
                        priorities.forEach { priority ->
                            DropdownMenuItem(
                                text = { Text(priority.name) },
                                onClick = {
                                    selectedPriorityId = priority.id
                                    expandedPriority = false
                                }
                            )
                        }
                    }
                }

                // Dropdown para grupo
                var expandedGroup by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedGroup,
                    onExpandedChange = { expandedGroup = !expandedGroup }
                ) {
                    OutlinedTextField(
                        value = groups.find { it.id == selectedGroupId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grupo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGroup) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGroup,
                        onDismissRequest = { expandedGroup = false }
                    ) {
                        groups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = {
                                    selectedGroupId = group.id
                                    expandedGroup = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (description.isNotBlank() && limitDate.isNotBlank() && limitTime.isNotBlank()) {
                        try {
                            val limitDateTime = LocalDateTime.parse("${limitDate}T${limitTime}:00")

                            // Crear la tarea actualizada
                            val updatedTask = Task(
                                id = task.id,
                                description = description,
                                create_date = task.create_date,
                                limit_time = limitDateTime,
                                finish_date = task.finish_date,
                                status = task.status,
                                id_priority = selectedPriorityId,
                                id_group = selectedGroupId
                            )

                            // Actualizar la tarea usando el viewModel
                            viewModel.updateTask(updatedTask)
                            onDismiss()

                        } catch (e: Exception) {
                            // Manejar error de formato de fecha/hora
                            // Podrías mostrar un toast o mensaje de error aquí
                        }
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun HomeScreenPreview() {
    Taller_MovilesTheme {
        HomeScreen(rememberNavController())
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun GroupScreenPreview() {
    Taller_MovilesTheme {
        GroupScreen(rememberNavController())
    }
}