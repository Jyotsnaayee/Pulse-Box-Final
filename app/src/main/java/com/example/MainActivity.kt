package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.AppDatabase
import com.example.data.FirestoreTaskRepository
import com.example.data.auth.AuthRepository
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.LoginViewModel
import com.example.ui.auth.LoginViewModelFactory
import com.example.ui.home.HomeScreen
import com.example.ui.tasks.TaskScreen
import com.example.ui.tasks.TaskViewModel
import com.example.ui.tasks.TaskViewModelFactory
import com.example.ui.theme.PulseBoxTheme
import com.example.ui.tools.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val authRepository = AuthRepository(this)
        val firestoreRepository = FirestoreTaskRepository()
        
        setContent {
            PulseBoxTheme {
                val currentUser by authRepository.currentUser.collectAsState()
                
                if (currentUser == null) {
                    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(authRepository))
                    LoginScreen(viewModel = loginViewModel, onSuccess = {})
                } else {
                    PulseBoxApp(firestoreRepository, authRepository)
                }
            }
        }
    }
}

@Composable
fun PulseBoxApp(repository: FirestoreTaskRepository, authRepository: AuthRepository) {
    val navController = rememberNavController()
    val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(repository))
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    onToolClick = { route ->
                        if (route == "sign_out") {
                            authRepository.signOut()
                        } else {
                            navController.navigate(route)
                        }
                    }
                )
            }
            composable("tasks") {
                TaskScreen(
                    viewModel = taskViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("calculator") { CalculatorScreen(onBack = { navController.popBackStack() }) }
            composable("unit_converter") { UnitConverterScreen(onBack = { navController.popBackStack() }) }
            composable("flashlight") { FlashlightScreen(onBack = { navController.popBackStack() }) }
            composable("qr_scanner") { QRScannerScreen(onBack = { navController.popBackStack() }) }
            composable("pdf_reader") { UniversalReaderScreen(onBack = { navController.popBackStack() }) }
            composable("file_reader") { UniversalReaderScreen(onBack = { navController.popBackStack() }) }
            composable("package_studio") { PackageStudioScreen(onBack = { navController.popBackStack() }) }
            composable("compressor") { UniversalReaderScreen(onBack = { navController.popBackStack() }) }
            composable("wallpaper") { WallpaperScreen(onBack = { navController.popBackStack() }) }
            composable("news") { NewsHubScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
