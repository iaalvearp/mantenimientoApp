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
            val databaseData: DatabaseJsonData = gson.fromJson(InputStreamReader(databaseStream), DatabaseJsonData::class.java)

            // Insertar todos los datos de database.json
            databaseData.roles.forEach { dao.insertarRol(it) }
            databaseData.usuarios.forEach { dao.insertarUsuario(it) }
            databaseData.estados.forEach { dao.insertarEstado(it) }
            databaseData.clientes.forEach { dao.insertarCliente(it) }

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
                    proyectoId = 1,
                    ciudadId = 1,
                    agenciaId = 1
                )
                dao.insertarTarea(tarea)
                tareaJson.equipos.forEach { equipoJson ->
                    dao.insertarEquipo(equipoJson.toEquipo(tarea.id))
                }
            }

            // Cargar actividadesMantenimiento.json
            val actividadesStream = assets.open("actividadesMantenimiento.json")
            val actividadesData: ActividadesJsonData = gson.fromJson(InputStreamReader(actividadesStream), ActividadesJsonData::class.java)
            actividadesData.actividadesPreventivo.forEach { actividadJson ->
                val actividad = ActividadMantenimiento(id = actividadJson.id, nombre = actividadJson.nombre, tipo = "preventivo")
                dao.insertarActividadMantenimiento(actividad)
                actividadJson.posiblesRespuestas.forEach { respuestaJson ->
                    val respuesta = PosibleRespuesta(id = respuestaJson.id, label = respuestaJson.label, value = respuestaJson.value, actividadId = actividad.id)
                    dao.insertarPosibleRespuesta(respuesta)
                }
            }
            actividadesData.actividadesCorrectivo.forEach { actividadJson ->
                val actividad = ActividadMantenimiento(id = actividadJson.id + 100, nombre = actividadJson.nombre, tipo = "correctivo")
                dao.insertarActividadMantenimiento(actividad)
                actividadJson.posiblesRespuestas.forEach { respuestaJson ->
                    val respuesta = PosibleRespuesta(id = respuestaJson.id + 100, label = respuestaJson.label, value = respuestaJson.value, actividadId = actividad.id)
                    dao.insertarPosibleRespuesta(respuesta)
                }
            }

            prefs.edit {
                putBoolean("is_first_run", false)
            }
        }
    }

    // --- DATA CLASSES CORREGIDAS PARA COINCIDIR 100% CON LOS JSON ---

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
        // --- CORRECCIÓN AQUÍ: Faltaba 'roles' ---
        val roles: List<Rol>,
        val usuarios: List<Usuario>,
        val estados: List<Estado>,
        val clientes: List<Cliente>
    )

    private data class ActividadesJsonData(
        val actividadesPreventivo: List<ActividadConRespuestasJson>,
        val actividadesCorrectivo: List<ActividadConRespuestasJson>
    )

    private data class ActividadConRespuestasJson(
        val id: Int,
        val nombre: String,
        val posiblesRespuestas: List<RespuestaJson>
    )

    private data class RespuestaJson(
        val id: Int,
        val label: String,
        val value: String
    )
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF33A8FF)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}