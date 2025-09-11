// Archivo: AppNavigation.kt
package com.alpes.mantenimientoapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // KOTLIN: 'startDestination' sigue siendo "login".
    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // TODO: Aquí iría la lógica real de login.
                    // Por ahora, asumimos que el usuario con ID 101 se logueó exitosamente.
                    navController.navigate("home/101") { // Pasamos el ID del usuario en la ruta.
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // KOTLIN: La ruta ahora espera un argumento, que llamamos "userId".
        composable(
            route = "home/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            // Extraemos el userId de los argumentos de la ruta.
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            HomeScreen(userId = userId) // Le pasamos el ID a la HomeScreen.
        }
    }
}