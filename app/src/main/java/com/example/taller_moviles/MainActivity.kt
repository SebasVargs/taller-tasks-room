package com.example.taller_moviles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taller_moviles.database.viewModels.TaskViewModel
import com.example.taller_moviles.ui.theme.Taller_MovilesTheme

class MainActivity : ComponentActivity() {

    private val taskDb by lazy { TaskViewModel(this) }

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

@Composable
fun HomeScreen(navController: NavController) {

}

@Composable
fun TaskScreen(navController: NavController) {

}

@Composable
fun GroupScreen(navController: NavController) {

}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun HomeScreenPreview() {
    Taller_MovilesTheme {
        HomeScreen(rememberNavController())
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun TaskScreenPreview() {
    Taller_MovilesTheme {
        TaskScreen(rememberNavController())
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun GroupScreenPreview() {
    Taller_MovilesTheme {
        GroupScreen(rememberNavController())
    }
}