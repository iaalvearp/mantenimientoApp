package com.alpes.mantenimientoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.alpes.mantenimientoapp.ui.theme.MantenimientoAppTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // KOTLIN/ANDROID: 'lifecycleScope.launch' inicia una corrutina.
        // Piensa en una corrutina como un "hilo de trabajo secundario".
        // Hacemos la carga de datos aquí para no congelar la pantalla principal de la app.
        lifecycleScope.launch(Dispatchers.IO) { // Dispatchers.IO es ideal para operaciones de archivos/red.
            prepopulateDatabaseIfNeeded()
        }

        setContent {
            MantenimientoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF33A8FF)
                ) {
                    // Por ahora, seguimos mostrando el LoginScreen.
                    // Más adelante lo cambiaremos por un sistema de navegación.
                    LoginScreen(
                        onLoginSuccess = { userId ->
                            // Cuando implementemos la navegación, usaremos este userId
                            // para ir a la pantalla de inicio correcta. Ejemplo:
                            // navController.navigate("home/${userId}")
                        }
                    )
                }
            }
        }
    }

    // KOTLIN: 'suspend fun' es una función especial que puede ser pausada y reanudada.
    // Solo puede ser llamada desde dentro de una corrutina.
    private suspend fun prepopulateDatabaseIfNeeded() {
        // ANDROID: SharedPreferences es un pequeño almacén para guardar datos simples,
        // como configuraciones. Lo usamos aquí para guardar una "bandera" y saber
        // si ya hemos cargado los datos previamente.
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("is_first_run", true)

        if (isFirstRun) {
            val database = AppDatabase.getDatabase(applicationContext)
            val dao = database.appDao()
            val gson = Gson()

            // --- Cargar Tareas y Equipos ---
            // Leemos el archivo tareas.json desde la carpeta 'assets'.
            val tareasStream = assets.open("tareas.json")
            // Usamos Gson para convertir el JSON en una lista de objetos Tarea.
            val tareaListType = object : TypeToken<List<TareaConEquipos>>() {}.type
            val tareasConEquipos: List<TareaConEquipos> = gson.fromJson(InputStreamReader(tareasStream), tareaListType)

            // Recorremos la lista y guardamos cada tarea y sus equipos.
            tareasConEquipos.forEach { tareaConEquipos ->
                dao.insertarTarea(tareaConEquipos.toTarea())
                tareaConEquipos.equipos.forEach { equipoJson ->
                    // Ajustamos el equipo para incluir el ID de la tarea a la que pertenece.
                    dao.insertarEquipo(equipoJson.toEquipo(tareaConEquipos.id))
                }
            }

            // --- Cargar Usuarios y Estados desde database.json ---
            val databaseStream = assets.open("database.json")
            val databaseJsonData: DatabaseJsonData = gson.fromJson(InputStreamReader(databaseStream), DatabaseJsonData::class.java)

            databaseJsonData.usuarios.forEach { dao.insertarUsuario(it) }
            databaseJsonData.estados.forEach { dao.insertarEstado(it) }

            // Marcamos la bandera para que este bloque no se vuelva a ejecutar.
            prefs.edit().putBoolean("is_first_run", false).apply()
        }
    }
}

// KOTLIN: Creamos clases de datos temporales que coinciden con la estructura de tus JSON.
// Esto ayuda a Gson a entender cómo leer los archivos.
private data class TareaConEquipos(
    val id: Int,
    val clienteId: Int,
    val provinciaId: Int,
    val unidadNegocioId: Int,
    val usuarioId: Int,
    val equipos: List<EquipoJson>
) {
    fun toTarea(): Tarea = Tarea(id, clienteId, provinciaId, unidadNegocioId, usuarioId)
}

private data class EquipoJson(
    val id: String,
    val nombre: String,
    val modelo: String,
    val caracteristicas: String,
    val estadoId: Int
) {
    fun toEquipo(tareaId: Int): Equipo = Equipo(id, nombre, modelo, caracteristicas, estadoId, tareaId)
}

private data class DatabaseJsonData(
    val usuarios: List<Usuario>,
    val estados: List<Estado>
    // Aquí podrías añadir las otras listas de tu database.json si las necesitas
)