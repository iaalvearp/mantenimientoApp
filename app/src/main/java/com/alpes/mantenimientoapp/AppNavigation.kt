// Archivo: AppNavigation.kt
package com.alpes.mantenimientoapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
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
                        // Esto previene que el usuario pueda volver a la pantalla de login
                        // presionando el botón de "atrás".
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

            // Pasamos la lógica de navegación a la HomeScreen
            HomeScreen(
                userId = userId,
                onLogout = {
                    // Esta es la orden que se ejecutará al hacer clic en "Cerrar Sesión"
                    navController.navigate("login") {
                        // Limpia todo el historial de navegación para que el usuario
                        // no pueda volver a la pantalla de inicio con el botón "atrás".
                        popUpTo(0)
                    }
                }
            )
        }
    }
}