// Archivo: AppNavigation.kt (REEMPLAZAR COMPLETO)
package com.alpes.mantenimientoapp

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // --- INICIO DE LA CORRECCIÓN 1: Pasar 'application' a la Factory ---
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).appDao()
    // Obtenemos la instancia de Application desde el contexto
    val application = context.applicationContext as Application
    // Y se la pasamos a la fábrica al crearla
    val viewModelFactory = ViewModelFactory(dao, application)
    // --- FIN DE LA CORRECCIÓN 1 ---


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
                onEquipoClicked = { equipoId, numeroSerie -> // Ahora pasamos ambos datos
                    // Navegamos a la pantalla de detalle
                    navController.navigate("taskDetail/$equipoId/$numeroSerie")
                },
                onAddEquipmentClicked = {
                    navController.navigate("addEquipment/$userId") // <-- AÑADE /$userId
                }
            )
        }

        composable(
            route = "taskDetail/{equipoId}/{numeroSerie}", // <-- RUTA ACTUALIZADA
            arguments = listOf(
                navArgument("equipoId") { type = NavType.StringType },
                navArgument("numeroSerie") { type = NavType.StringType } // <-- ARGUMENTO AÑADIDO
            )
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getString("equipoId") ?: ""
            val numeroSerie = backStackEntry.arguments?.getString("numeroSerie") ?: "" // <-- OBTENEMOS EL VALOR
            val taskDetailViewModel: TaskDetailViewModel = viewModel(factory = viewModelFactory)


            TaskDetailScreen(
                equipoId = equipoId,
                numeroSerie = numeroSerie, // 2. Se lo pasamos a la pantalla
                viewModel = taskDetailViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNextClicked = {
                    navController.navigate("maintenanceActivities/$equipoId/$numeroSerie")
                }
            )
        }

        composable(
            route = "maintenanceActivities/{equipoId}/{numeroSerie}",
            arguments = listOf(
                navArgument("equipoId") { type = NavType.StringType },
                navArgument("numeroSerie") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getString("equipoId") ?: ""
            val numeroSerie = backStackEntry.arguments?.getString("numeroSerie") ?: ""

            // 1. Obtenemos el ViewModel (sin cambios)
            val taskDetailViewModel: TaskDetailViewModel = viewModel(factory = viewModelFactory)

            // --- INICIO DE CAMBIOS (Req #6a) ---
            // 2. Obtenemos el ciclo de vida actual
            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

            // 3. Creamos un 'Efecto' que se suscribe a los eventos del ciclo de vida
            LaunchedEffect(key1 = lifecycleOwner) {
                lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                    // 4. CADA VEZ que la pantalla vuelve a estar en 'ON_RESUME' (visible)
                    override fun onResume(owner: LifecycleOwner) {
                        // 5. Le decimos al ViewModel que recargue los datos de la BD
                        taskDetailViewModel.loadDataForEquipo(equipoId)
                    }
                })
            }
            // --- FIN DE CAMBIOS (Req #6a) ---

            // 3. Recogemos el estado (UiState) (sin cambios)
            val uiState by taskDetailViewModel.uiState.collectAsStateWithLifecycle()

            MaintenanceActivitiesScreen(
                onNavigateBack = { navController.popBackStack() },
                onPreventiveClicked = { navController.navigate("preventiveChecklist/$equipoId") },
                onCorrectiveClicked = { navController.navigate("correctiveChecklist/$equipoId") },
                onNextClicked = { eqId, numSerie ->
                    navController.navigate("finalizacion/$eqId/$numSerie")
                },
                equipoId = equipoId,
                numeroSerie = numeroSerie,
                isPreventiveEnabled = !uiState.isCorrectiveCompleted,
                isCorrectiveEnabled = !uiState.isPreventiveCompleted
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

            // --- INICIO DE CAMBIOS (Req #8) ---
            // Escuchamos la señal de navegación del ViewModel
            LaunchedEffect(key1 = Unit) {
                checklistViewModel.navigateToDiagnostic.collect { idEquipoGuardado ->
                    // Navegamos al diagnóstico y limpiamos la pila para no volver aquí
                    navController.navigate("diagnosticoChecklist/$idEquipoGuardado") {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                    }
                }
            }
            // --- FIN DE CAMBIOS (Req #8) ---

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

            // --- INICIO DE CAMBIOS (Req #8) ---
            // Escuchamos la señal de navegación del ViewModel
            LaunchedEffect(key1 = Unit) {
                checklistViewModel.navigateToDiagnostic.collect { idEquipoGuardado ->
                    // Navegamos al diagnóstico y limpiamos la pila para no volver aquí
                    navController.navigate("diagnosticoChecklist/$idEquipoGuardado") {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                    }
                }
            }
            // --- FIN DE CAMBIOS (Req #8) ---

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

        // --- AÑADE ESTA NUEVA RUTA ---
        composable(
            route = "finalizacion/{equipoId}/{numeroSerie}", // <-- RUTA ACTUALIZADA
            arguments = listOf(
                navArgument("equipoId") { type = NavType.StringType },
                navArgument("numeroSerie") { type = NavType.StringType } // <-- ARGUMENTO AÑADIDO
            )
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getString("equipoId") ?: ""
            val numeroSerie = backStackEntry.arguments?.getString("numeroSerie") ?: "" // <-- OBTENEMOS EL VALOR
            val viewModel: FinalizacionViewModel = viewModel(factory = viewModelFactory)

            FinalizacionScreen(
                equipoId = equipoId,
                numeroSerie = numeroSerie, // <-- PASAMOS EL VALOR
                viewModel = viewModel,
                onNavigateBackToHome = { userId ->
                    navController.navigate("home/$userId") {
                        popUpTo("home/$userId") { inclusive = true }
                    }
                },
                onBackClicked = { navController.popBackStack() }
            )
        }

        composable(
            route = "addEquipment/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val viewModel: AddEquipmentViewModel = viewModel(factory = viewModelFactory)
            AddEquipmentScreen(
                viewModel = viewModel,
                userId = userId,
                onNavigateBack = { navController.popBackStack() },
                onEquipoClicked = { equipoId, numeroSerie ->
                    navController.navigate("taskDetail/$equipoId/$numeroSerie")
                }
            )
        }
    }
}