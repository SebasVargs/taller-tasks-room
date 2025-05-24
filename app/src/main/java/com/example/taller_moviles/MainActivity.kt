package com.example.taller_moviles

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
        composable("task") {
            TaskScreen(navController)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Tareas") },
                actions = {
                    IconButton(onClick = { navController.navigate("task") }) {
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

            // Lista de tareas agrupadas
            val groupedTasks = tasks.groupBy { it.group_name }

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

                    items(groupTasks.sortedBy { it.limit_time }) { task ->
                        TaskItem(
                            task = task,
                            onFinish = { viewModel.finishTask(it) },
                            onDelete = {
                                val taskEntity = Task(
                                    task.id, task.description, task.create_date,
                                    task.limit_time, task.finish_date, task.status,
                                    task.id_priority, task.id_group
                                )
                                viewModel.deleteTask(taskEntity)
                            }
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
fun TaskScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel = remember { TaskViewModel(context) }

    var description by remember { mutableStateOf("") }
    var selectedPriorityId by remember { mutableIntStateOf(1) }
    var selectedGroupId by remember { mutableIntStateOf(1) }
    var limitDate by remember { mutableStateOf("") }
    var limitTime by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val priorities by viewModel.priorities.collectAsState(initial = emptyList())
    val groups by viewModel.groups.collectAsState(initial = emptyList())
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Tareas") },
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
            // Formulario para agregar tarea
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Nueva Tarea",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = limitDate,
                            onValueChange = { limitDate = it },
                            label = { Text("Fecha (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = limitTime,
                            onValueChange = { limitTime = it },
                            label = { Text("Hora (HH:MM)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Dropdown para prioridad
                        var expandedPriority by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expandedPriority,
                            onExpandedChange = { expandedPriority = !expandedPriority },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = priorities.find { it.id == selectedPriorityId }?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Prioridad") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority) },
                                modifier = Modifier.menuAnchor()
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
                            onExpandedChange = { expandedGroup = !expandedGroup },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = groups.find { it.id == selectedGroupId }?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Grupo") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGroup) },
                                modifier = Modifier.menuAnchor()
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (description.isNotBlank() && limitDate.isNotBlank() && limitTime.isNotBlank()) {
                                try {
                                    val limitDateTime = LocalDateTime.parse("${limitDate}T${limitTime}:00")
                                    viewModel.addTask(description, limitDateTime, selectedPriorityId, selectedGroupId)
                                    description = ""
                                    limitDate = ""
                                    limitTime = ""
                                } catch (e: Exception) {
                                    // Manejar error de formato de fecha
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Agregar Tarea")
                    }
                }
            }

            // Búsqueda de tareas
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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de tareas
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

                    items(groupTasks.sortedBy { it.limit_time }) { task ->
                        TaskItem(
                            task = task,
                            onFinish = { viewModel.finishTask(it) },
                            onDelete = {
                                val taskEntity = Task(
                                    task.id, task.description, task.create_date,
                                    task.limit_time, task.finish_date, task.status,
                                    task.id_priority, task.id_group
                                )
                                viewModel.deleteTask(taskEntity)
                            }
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
fun GroupScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel = remember { TaskViewModel(context) }

    var groupName by remember { mutableStateOf("") }
    val groups by viewModel.groups.collectAsState(initial = emptyList())

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
                                onClick = { viewModel.deleteGroup(group) }
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
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskItem(
    task: TaskWithDetails,
    onFinish: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val isOverdue = !task.status && task.limit_time.isBefore(LocalDateTime.now())
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                            "Importante" -> Color.Red
                            "Toca hacerla" -> Color.Blue
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
fun TaskScreenPreview() {
    Taller_MovilesTheme {
        TaskScreen(rememberNavController())
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