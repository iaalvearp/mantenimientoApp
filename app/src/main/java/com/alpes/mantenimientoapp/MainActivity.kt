package com.alpes.mantenimientoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.alpes.mantenimientoapp.ui.theme.MantenimientoAppTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {

    // 1. Creamos un estado para controlar si la app está cargando los datos iniciales.
    private var isLoading by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lanzamos la corrutina para preparar la base de datos.
        lifecycleScope.launch(Dispatchers.IO) {
            prepopulateDatabaseIfNeeded()
            // Cuando termina, actualizamos el estado en el hilo principal.
            runOnUiThread {
                isLoading = false
            }
        }

        setContent {
            MantenimientoAppTheme {
                // 2. Decidimos qué mostrar basado en el estado de carga.
                if (isLoading) {
                    LoadingScreen()
                } else {
                    // Una vez que la carga termina, mostramos la navegación normal.
                    AppNavigation()
                }
            }
        }
    }

    private suspend fun prepopulateDatabaseIfNeeded() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("is_first_run", true)
        if (isFirstRun) {
            val database = AppDatabase.getDatabase(applicationContext)
            val dao = database.appDao()
            val gson = Gson()

            // Cargar Tareas y Equipos
            val tareasStream = assets.open("tareas.json")
            val tareaListType = object : TypeToken<List<TareaConEquipos>>() {}.type
            val tareasConEquipos: List<TareaConEquipos> = gson.fromJson(InputStreamReader(tareasStream), tareaListType)
            tareasConEquipos.forEach { tareaConEquipos ->
                dao.insertarTarea(tareaConEquipos.toTarea())
                tareaConEquipos.equipos.forEach { equipoJson ->
                    dao.insertarEquipo(equipoJson.toEquipo(tareaConEquipos.id))
                }
            }

            // Cargar Usuarios y Estados
            val databaseStream = assets.open("database.json")
            val databaseJsonData: DatabaseJsonData = gson.fromJson(InputStreamReader(databaseStream), DatabaseJsonData::class.java)
            databaseJsonData.usuarios.forEach { dao.insertarUsuario(it) }
            databaseJsonData.estados.forEach { dao.insertarEstado(it) }

            // Usamos la extensión KTX que es más limpia
            prefs.edit {
                putBoolean("is_first_run", false)
            }
        }
    }

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
    )
}

// 3. Creamos un Composable simple para la pantalla de carga.
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF33A8FF)), // Puedes usar el color de fondo que prefieras
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}