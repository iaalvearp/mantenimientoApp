// Archivo: MainActivity.kt
package com.alpes.mantenimientoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    private var startupError by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                prepopulateDatabaseIfNeeded()
            } catch (e: Exception) {
                e.printStackTrace()
                startupError = "Error al iniciar la app:\n\n${e.javaClass.simpleName}\n${e.message}"
            } finally {
                runOnUiThread { isLoading = false }
            }
        }
        setContent {
            MantenimientoAppTheme {
                when {
                    startupError != null -> ErrorDialog(errorMessage = startupError!!) { finish() }
                    isLoading -> LoadingScreen()
                    else -> AppNavigation()
                }
            }
        }
    }

    private suspend fun prepopulateDatabaseIfNeeded() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("is_first_run", true)

        if (isFirstRun) {
            val dao = AppDatabase.getDatabase(applicationContext).appDao()
            val gson = Gson()

            // --- 1 y 2: Carga de database.json y tareas.json (SIN CAMBIOS) ---
            // (Todo el código anterior para cargar usuarios, equipos, tareas, etc., se mantiene igual)
            val dbStream = assets.open("database.json")
            val dbData: DatabaseJsonData = gson.fromJson(InputStreamReader(dbStream), DatabaseJsonData::class.java)
            dbData.roles.forEach { dao.insertarRol(it) }
            dbData.usuarios.forEach { dao.insertarUsuario(it) }
            dbData.estados.forEach { dao.insertarEstado(it) }
            dbData.unidadesNegocio.forEach { dao.insertarUnidadNegocio(it) }
            dbData.clientes.forEach { clienteJson ->
                dao.insertarCliente(Cliente(clienteJson.id, clienteJson.nombre, clienteJson.nombreCompleto))
                clienteJson.proyectos.forEach { proyectoJson ->
                    dao.insertarProyecto(Proyecto(proyectoJson.id, proyectoJson.nombre, clienteJson.id))
                }
            }
            dbData.ubicacion.forEach { ubiJson ->
                dao.insertarProvincia(Provincia(ubiJson.id, ubiJson.provincia))
                ubiJson.ciudades.forEach { ciudadJson ->
                    dao.insertarCiudad(Ciudad(ciudadJson.id, ciudadJson.nombre, ubiJson.id))
                    ciudadJson.agencia.forEach { agenciaJson ->
                        dao.insertarAgencia(Agencia(agenciaJson.id, agenciaJson.nombre, agenciaJson.unidadNegocioId, ciudadJson.id))
                    }
                }
            }
            dbData.tiposEquipos.forEach { tipoEquipo ->
                tipoEquipo.equipos.forEach { equipoJson ->
                    dao.insertarEquipo(
                        Equipo(
                            id = equipoJson.id, nombre = equipoJson.nombre, modelo = equipoJson.modelo,
                            caracteristicas = equipoJson.caracteristicas, estadoId = 1, tareaId = 0, creadoPorUsuarioId = null
                        )
                    )
                }
            }
            val tareasStream = assets.open("tareas.json")
            val tareaListType = object : TypeToken<List<TareaAsignadaJson>>() {}.type
            val tareasAsignadasJson: List<TareaAsignadaJson> = gson.fromJson(InputStreamReader(tareasStream), tareaListType)
            val equipoAdminDataMap = dbData.tiposEquipos.flatMap { grupo ->
                grupo.equipos.map { equipo -> equipo.id to grupo }
            }.toMap()
            tareasAsignadasJson.forEach { tareaJson ->
                val adminData = equipoAdminDataMap[tareaJson.idEquipo]
                if (adminData != null) {
                    val nuevaTarea = Tarea(
                        id = tareaJson.id, usuarioId = tareaJson.usuarioId, clienteId = adminData.clienteId,
                        proyectoId = adminData.proyectoId, provinciaId = adminData.provinciaId,
                        ciudadId = adminData.ciudadId, unidadNegocioId = adminData.unidadNegocioId, agenciaId = adminData.agenciaId
                    )
                    dao.insertarTarea(nuevaTarea)
                    dao.vincularEquipoConTarea(equipoId = tareaJson.idEquipo, tareaId = nuevaTarea.id)
                }
            }

            // --- 3. CARGA DE ACTIVIDADES DE MANTENIMIENTO (LÓGICA ACTUALIZADA) ---
            val actividadesStream = assets.open("actividadesMantenimiento.json")
            val actividadesData: ActividadesJsonData = gson.fromJson(InputStreamReader(actividadesStream), ActividadesJsonData::class.java)

            // Esta función ahora funciona para los TRES tipos por igual
            suspend fun procesarActividades(lista: List<ActividadJson>, tipo: String) {
                lista.forEach { actJson ->
                    val nuevoActividadDbId = dao.insertarActividadMantenimiento(
                        ActividadMantenimiento(
                            id = actJson.id,
                            nombre = actJson.nombre,
                            tipo = tipo,
                            tipoSeleccion = actJson.tipoSeleccion
                        )
                    )

                    val respuestasAnidadas = actJson.posiblesRespuestas.firstOrNull()

                    respuestasAnidadas?.answerYes?.forEach { respJson ->
                        dao.insertarPosibleRespuesta(
                            PosibleRespuesta(
                                id = respJson.id,
                                label = respJson.label,
                                value = respJson.value,
                                actividadId = nuevoActividadDbId.toInt(),
                                esParaRespuestaAfirmativa = true
                            )
                        )
                    }

                    respuestasAnidadas?.answerNo?.forEach { respJson ->
                        dao.insertarPosibleRespuesta(
                            PosibleRespuesta(
                                id = respJson.id,
                                label = respJson.label,
                                value = respJson.value,
                                actividadId = nuevoActividadDbId.toInt(),
                                esParaRespuestaAfirmativa = false
                            )
                        )
                    }
                }
            }

            // La llamada a la función se mantiene, pero ahora procesará el JSON anidado
            procesarActividades(actividadesData.actividadesPreventivo, "preventivo")
            procesarActividades(actividadesData.actividadesCorrectivo, "correctivo")
            procesarActividades(actividadesData.actividadesDiagnostico, "diagnostico")

            prefs.edit { putBoolean("is_first_run", false) }
        }
    }

    // --- DATA CLASSES INTERNAS PARA LEER LOS NUEVOS JSON ---
    private data class TareaAsignadaJson(val id: Int, val usuarioId: Int, val idEquipo: String)
    private data class EquipoJson(val id: String, val nombre: String, val modelo: String, val caracteristicas: String)
    private data class TipoEquipoJson(
        val id: Int, val clienteId: Int, val proyectoId: Int, val provinciaId: Int, val ciudadId: Int,
        val unidadNegocioId: Int, val agenciaId: Int, val equipos: List<EquipoJson>
    )
    private data class ProyectoJson(val id: Int, val nombre: String)
    private data class ClienteJson(val id: Int, val nombre: String, val nombreCompleto: String, val proyectos: List<ProyectoJson>)
    private data class AgenciaJson(val id: Int, val nombre: String, val unidadNegocioId: Int)
    private data class CiudadJson(val id: Int, val nombre: String, val agencia: List<AgenciaJson>)
    private data class UbicacionJson(val id: Int, val provincia: String, val ciudades: List<CiudadJson>)
    private data class DatabaseJsonData(
        val roles: List<Rol>,
        val usuarios: List<Usuario>,
        val clientes: List<ClienteJson>,
        val unidadesNegocio: List<UnidadNegocio>,
        val ubicacion: List<UbicacionJson>,
        val tiposEquipos: List<TipoEquipoJson>,
        val estados: List<Estado>
    )

    // Representa una sola respuesta posible dentro de las listas answerYes/answerNo
    private data class RespuestaJson(val id: Int, val label: String, val value: String)

    // Representa el objeto que contiene las listas de respuestas para Sí y No
    private data class PosiblesRespuestasJson(
        val answerYes: List<RespuestaJson>,
        val answerNo: List<RespuestaJson>
    )

    // Representa una sola actividad dentro del JSON, ahora adaptada a la nueva estructura
    private data class ActividadJson(
        val id: Int,
        val nombre: String,
        @com.google.gson.annotations.SerializedName("type")
        val tipoSeleccion: String,
        val posiblesRespuestas: List<PosiblesRespuestasJson> // <-- CAMBIO IMPORTANTE AQUÍ
    )

    // Representa la estructura completa del archivo actividadesMantenimiento.json
    private data class ActividadesJsonData(
        val actividadesPreventivo: List<ActividadJson>,
        val actividadesCorrectivo: List<ActividadJson>,
        @com.google.gson.annotations.SerializedName("tareasDiagnostico")
        val actividadesDiagnostico: List<ActividadJson>
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

@Composable
fun ErrorDialog(errorMessage: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ocurrió un Problema") },
        text = { Text(errorMessage) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Aceptar")
            }
        }
    )
}