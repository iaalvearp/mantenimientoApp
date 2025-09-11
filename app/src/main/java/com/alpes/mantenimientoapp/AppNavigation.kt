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
            // 3. Creamos el ViewModel usando la fábrica y se lo pasamos a la pantalla.
            val loginViewModel: LoginViewModel = viewModel(factory = viewModelFactory)

            LoginScreen(
                loginViewModel = loginViewModel, // <-- PARÁMETRO AÑADIDO
                onLoginSuccess = { userId ->
                    // Cuando el login es exitoso (notificado por el ViewModel), navegamos.
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
            HomeScreen(userId = userId)
        }
    }
}