// Archivo: AppNavigation.kt
package com.alpes.mantenimientoapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // --- CORRECCIÓN ---
    // 1. Obtenemos el contexto actual para poder acceder a la base de datos.
    val context = LocalContext.current
    // 2. Creamos la "fábrica" que sabe cómo construir nuestro LoginViewModel.
    val dao = AppDatabase.getDatabase(context).appDao()
    val viewModelFactory = ViewModelFactory(dao)
    // --- FIN DE LA CORRECCIÓN ---


    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            val loginViewModel: LoginViewModel = viewModel(factory = viewModelFactory)

            LoginScreen(
                loginViewModel = loginViewModel,
                onLoginSuccess = { userId ->
                    // ✅ CORRECCIÓN FINAL: Reemplazamos el println con la navegación real.
                    navController.navigate("home/$userId") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "home/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)

            // Pasamos la lógica de navegación a la HomeScreen
            HomeScreen(
                userId = userId,
                homeViewModel = homeViewModel,
                onLogout = {
                    navController.navigate("login") { popUpTo(0) }
                },
                onEquipoClicked = { equipoId ->
                    // Cuando se hace clic en un equipo, navegamos a la pantalla de detalle
                    navController.navigate("taskDetail/$equipoId")
                }
            )
        }

        composable(
            route = "taskDetail/{equipoId}",
            arguments = listOf(navArgument("equipoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getString("equipoId") ?: ""
            val taskDetailViewModel: TaskDetailViewModel = viewModel(factory = viewModelFactory)

            TaskDetailScreen(
                equipoId = equipoId,
                viewModel = taskDetailViewModel,
                onNavigateBack = { navController.popBackStack() },
                // Le decimos qué hacer cuando se presione "SIGUIENTE"
                onNextClicked = {
                    navController.navigate("maintenanceActivities")
                }
            )
        }

        composable("maintenanceActivities") {
            MaintenanceActivitiesScreen(
                onNavigateBack = { navController.popBackStack() },
                onPreventiveClicked = {
                    // Navegamos a la nueva pantalla de checklist
                    navController.navigate("preventiveChecklist")
                },
                onCorrectiveClicked = { /* TODO */ },
                onNextClicked = { /* TODO */ }
            )
        }

// --- NUEVA RUTA PARA LA CHECKLIST ---
        composable("preventiveChecklist") {
            val checklistViewModel: ChecklistViewModel = viewModel(factory = viewModelFactory)
            PreventiveChecklistScreen(
                viewModel = checklistViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}