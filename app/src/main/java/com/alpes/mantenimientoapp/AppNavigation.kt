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
                    navController.navigate("maintenanceActivities/$equipoId")
                }
            )
        }

        composable(
            route = "maintenanceActivities/{equipoId}",
            arguments = listOf(navArgument("equipoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getString("equipoId") ?: ""
            MaintenanceActivitiesScreen(
                onNavigateBack = { navController.popBackStack() },
                onPreventiveClicked = {navController.navigate("preventiveChecklist/$equipoId") },
                onCorrectiveClicked = { navController.navigate("correctiveChecklist/$equipoId") },
                onDiagnosticoClicked = { navController.navigate("diagnosticoChecklist/$equipoId") }, // <-- Conectado aquí
                onNextClicked = { /* TODO */ }
            )
        }

        composable(
            route = "diagnosticoChecklist/{equipoId}",
            arguments = listOf(navArgument("equipoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getString("equipoId") ?: ""
            val checklistViewModel: ChecklistViewModel = viewModel(factory = viewModelFactory)
            PreventiveChecklistScreen(
                equipoId = equipoId,
                viewModel = checklistViewModel,
                onNavigateBack = { navController.popBackStack() },
                title = "Diagnóstico",
                checklistType = "diagnostico"
            )
        }

// --- NUEVA RUTA PARA LA CHECKLIST ---
        composable(
            route = "preventiveChecklist/{equipoId}",
            arguments = listOf(navArgument("equipoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getString("equipoId") ?: ""
            val checklistViewModel: ChecklistViewModel = viewModel(factory = viewModelFactory)
            PreventiveChecklistScreen(
                equipoId = equipoId,
                viewModel = checklistViewModel,
                onNavigateBack = { navController.popBackStack() },
                // --- LÍNEAS AÑADIDAS ---
                title = "Checklist Preventivo",
                checklistType = "preventivo"
            )
        }

        composable(
            route = "correctiveChecklist/{equipoId}",
            arguments = listOf(navArgument("equipoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getString("equipoId") ?: ""
            val checklistViewModel: ChecklistViewModel = viewModel(factory = viewModelFactory)

            // ¡Reutilizamos la misma pantalla!
            PreventiveChecklistScreen(
                equipoId = equipoId,
                viewModel = checklistViewModel,
                onNavigateBack = { navController.popBackStack() },
                // La única diferencia es el título que le pasaremos
                title = "Checklist Correctivo",
                checklistType = "correctivo" // Y el tipo de checklist a cargar
            )
        }
    }
}