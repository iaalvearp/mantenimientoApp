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

    private var isLoading by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            prepopulateDatabaseIfNeeded()
            runOnUiThread {
                isLoading = false
            }
        }

        setContent {
            MantenimientoAppTheme {
                if (isLoading) {
                    LoadingScreen()
                } else {
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

            // Cargar database.json
            val databaseStream = assets.open("database.json")
            val databaseJsonData: DatabaseJsonData = gson.fromJson(InputStreamReader(databaseStream), DatabaseJsonData::class.java)

            databaseJsonData.usuarios.forEach { dao.insertarUsuario(it) }
            databaseJsonData.estados.forEach { dao.insertarEstado(it) }
            databaseJsonData.clientes.forEach { dao.insertarCliente(it) }
            // Asumimos que no hay una tabla de Proyectos por ahora
            // databaseJsonData.proyectos.forEach { dao.insertarProyecto(it) }
            databaseJsonData.ubicacion.forEach { ubi ->
                dao.insertarProvincia(Provincia(id = ubi.id, nombre = ubi.provincia))
            }

            // Cargar tareas.json
            val tareasStream = assets.open("tareas.json")
            val tareaListType = object : TypeToken<List<TareaJson>>() {}.type
            val tareasJson: List<TareaJson> = gson.fromJson(InputStreamReader(tareasStream), tareaListType)

            tareasJson.forEach { tareaJson ->
                val tarea = Tarea(
                    id = tareaJson.id,
                    clienteId = tareaJson.clienteId,
                    provinciaId = tareaJson.provinciaId,
                    unidadNegocioId = tareaJson.unidadNegocioId,
                    usuarioId = tareaJson.usuarioId,
                    proyectoId = 1, // Default value
                    ciudadId = 1,   // Default value
                    agenciaId = 1   // Default value
                )
                dao.insertarTarea(tarea)
                tareaJson.equipos.forEach { equipoJson ->
                    dao.insertarEquipo(equipoJson.toEquipo(tarea.id))
                }
            }

            // Cargar Actividades de Mantenimiento
            val actividadesStream = assets.open("actividadesMantenimiento.json")
            val actividadesData: ActividadesJsonData = gson.fromJson(InputStreamReader(actividadesStream), ActividadesJsonData::class.java)
            actividadesData.actividades.forEach { dao.insertarActividadMantenimiento(it) }
            actividadesData.posiblesRespuestas.forEach { dao.insertarPosibleRespuesta(it) }

            prefs.edit {
                putBoolean("is_first_run", false)
            }
        }
    }

    // --- DATA CLASSES CORREGIDAS PARA COINCIDIR CON LOS JSON ---

    private data class TareaJson(
        val id: Int,
        val clienteId: Int,
        val provinciaId: Int,
        val unidadNegocioId: Int,
        val usuarioId: Int,
        val equipos: List<EquipoJson>
    )

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
        val estados: List<Estado>,
        val clientes: List<Cliente>,
        val ubicacion: List<UbicacionJson>
        // val proyectos: List<Proyecto> // Comentado ya que no hay tabla Proyecto en la BD
    )

    private data class UbicacionJson(
        val id: Int,
        val provincia: String
    )

    private data class ActividadesJsonData(
        val posiblesRespuestas: List<PosibleRespuesta>,
        val actividades: List<ActividadMantenimiento>
    )
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF33A8FF)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}