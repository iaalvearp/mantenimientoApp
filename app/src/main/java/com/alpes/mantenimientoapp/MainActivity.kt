package com.alpes.mantenimientoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    // --- INICIO DE LA "CAJA NEGRA" ---
    // Variable para guardar el mensaje de error si algo falla.
    private var startupError by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            // --- INICIO DE LA "CAJA NEGRA" ---
            try {
                // Intentamos preparar la base de datos como siempre.
                prepopulateDatabaseIfNeeded()
            } catch (e: Exception) {
                // Si algo falla, guardamos el mensaje de error para mostrarlo en la UI.
                // e.printStackTrace() es útil para ver el error completo si tuvieras Logcat.
                e.printStackTrace()
                startupError = "Error al iniciar la app:\n\n${e.message}"
            } finally {
                // Ocurra un error o no, dejamos de mostrar la pantalla de carga.
                runOnUiThread {
                    isLoading = false
                }
            }
            // --- FIN DE LA "CAJA NEGRA" ---
        }

        setContent {
            MantenimientoAppTheme {
                // --- LÓGICA DE LA UI ACTUALIZADA ---
                when {
                    // Si hay un error, lo mostramos primero que nada.
                    startupError != null -> {
                        ErrorDialog(errorMessage = startupError!!) {
                            // Cerramos la app al presionar OK, ya que no puede continuar.
                            finish()
                        }
                    }
                    // Si no hay error y está cargando, mostramos el Círculo de Carga.
                    isLoading -> {
                        LoadingScreen()
                    }
                    // Si no hay error y no está cargando, mostramos la app.
                    else -> {
                        AppNavigation()
                    }
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

            // --- 1. CARGA DE DATOS GENERALES Y CREACIÓN DE MAPAS PARA BÚSQUEDA ---
            val databaseStream = assets.open("database.json")
            val databaseData: DatabaseJsonData = gson.fromJson(InputStreamReader(databaseStream), DatabaseJsonData::class.java)

            databaseData.roles.forEach { dao.insertarRol(it) }
            databaseData.usuarios.forEach { dao.insertarUsuario(it) }
            databaseData.estados.forEach { dao.insertarEstado(it) }
            databaseData.clientes.forEach { dao.insertarCliente(it) }

            // ¡NUEVO! Leemos los proyectos del JSON y los insertamos
            databaseData.proyectos.forEach { dao.insertarProyecto(it) }

            // Creamos un mapa para buscar Agencias fácilmente después
            val unidadNegocioMap = mutableMapOf<Int, UnidadNegocioJson>()

            databaseData.ubicacion.forEach { ubi ->
                dao.insertarProvincia(Provincia(id = ubi.id, nombre = ubi.provincia))
                ubi.ciudades.forEach { ciudadJson ->
                    // CORRECCIÓN 1: Pasar el provinciaId a la Ciudad
                    dao.insertarCiudad(Ciudad(id = ciudadJson.id, nombre = ciudadJson.nombre, provinciaId = ubi.id))
                }
                ubi.unidadNegocio.forEach { unJson ->
                    // CORRECCIÓN 2: Pasar el provinciaId a la UnidadNegocio
                    dao.insertarUnidadNegocio(UnidadNegocio(id = unJson.id, nombre = unJson.nombre, ciudadId = unJson.ciudad, provinciaId = ubi.id))

                    // El resto del código se mantiene igual
                    unidadNegocioMap[unJson.id] = unJson
                    unJson.agencia.forEach { agenciaJson ->
                        dao.insertarAgencia(Agencia(id = agenciaJson.id, nombre = agenciaJson.nombre, unidadNegocioId = unJson.id))
                    }
                }
            }

            // --- 2. CARGA DE ACTIVIDADES DE MANTENIMIENTO ---
            val actividadesStream = assets.open("actividadesMantenimiento.json")
            val actividadesData: ActividadesJsonData = gson.fromJson(InputStreamReader(actividadesStream), ActividadesJsonData::class.java)

            // Función auxiliar para no repetir código
            suspend fun insertarActividades(lista: List<ActividadConRespuestasJson>, tipo: String, idOffset: Int) {
                lista.forEach { actividadJson ->
                    val actividad = ActividadMantenimiento(
                        id = actividadJson.id + idOffset,
                        nombre = actividadJson.nombre,
                        tipo = tipo,
                        tipoSeleccion = actividadJson.type // <-- AÑADE ESTA LÍNEA
                    )
                    dao.insertarActividadMantenimiento(actividad)
                    actividadJson.posiblesRespuestas.forEachIndexed { index, respuestaJson ->
                        // Creamos un ID único y predecible para cada posible respuesta
                        val prId = (actividad.id * 100) + index
                        dao.insertarPosibleRespuesta(PosibleRespuesta(id = prId, label = respuestaJson.label, value = respuestaJson.value, actividadId = actividad.id))
                    }
                }
            }

            insertarActividades(actividadesData.actividadesPreventivo, "preventivo", 0)
            insertarActividades(actividadesData.actividadesCorrectivo, "correctivo", 100)

            // --- AÑADE ESTA LÍNEA ---
            insertarActividades(actividadesData.tareasDiagnostico, "diagnostico", 200)


            // --- 3. CARGA INTELIGENTE DE TAREAS ---
            val tareasStream = assets.open("tareas.json")
            val tareaListType = object : TypeToken<List<TareaJson>>() {}.type
            val tareasJson: List<TareaJson> = gson.fromJson(InputStreamReader(tareasStream), tareaListType)

            tareasJson.forEach { tareaJson ->
                // Buscamos la unidad de negocio en nuestro mapa para encontrar la agencia
                val unidadNegocio = unidadNegocioMap[tareaJson.unidadNegocioId]

                // Deducimos la agenciaId. Usamos '1' como valor de respaldo si algo falla.
                val agenciaIdDeducida = unidadNegocio?.agencia?.firstOrNull()?.id ?: 1

                // Asumimos que todas las tareas pertenecen al único proyecto que tenemos.
                // Si hubiera más, aquí iría una lógica para buscar el proyecto correcto.
                val proyectoIdAsignado = 1

                val tarea = Tarea(
                    id = tareaJson.id,
                    clienteId = tareaJson.clienteId,
                    provinciaId = tareaJson.provinciaId,
                    unidadNegocioId = tareaJson.unidadNegocioId,
                    usuarioId = tareaJson.usuarioId,
                    // --- VALORES CORREGIDOS Y DINÁMICOS ---
                    proyectoId = proyectoIdAsignado,
                    ciudadId = tareaJson.ciudadId,      // Usamos el valor real del JSON
                    agenciaId = agenciaIdDeducida       // Usamos el valor que deducimos
                )
                dao.insertarTarea(tarea)
                tareaJson.equipos.forEach { equipoJson ->
                    dao.insertarEquipo(equipoJson.toEquipo(tarea.id))
                }
            }

            prefs.edit { putBoolean("is_first_run", false) }
        }
    }

    // --- DATA CLASSES INTERNAS PARA LEER LOS JSON ---

    private data class TareaJson(
        val id: Int,
        val clienteId: Int,
        val provinciaId: Int,
        val ciudadId: Int, // <-- CAMPO AÑADIDO Y NECESARIO
        val unidadNegocioId: Int,
        val usuarioId: Int,
        val equipos: List<EquipoJson>
    )

    private data class EquipoJson(
        val id: String,
        val nombre: String,
        val modelo: String,
        val caracteristicas: String,
        val estadoId: Int // Mantenemos este campo para que el JSON se lea sin errores
    ) {
        // --- ¡LA MAGIA ESTÁ AQUÍ! ---
        // Ignoramos el 'estadoId' del JSON y siempre usamos '1' (Pendiente).
        fun toEquipo(tareaId: Int): Equipo = Equipo(id, nombre, modelo, caracteristicas, 1, tareaId, syncPending = false)
    }

    private data class DatabaseJsonData(
        val roles: List<Rol>,
        val usuarios: List<Usuario>,
        val clientes: List<Cliente>,
        val proyectos: List<Proyecto>, // <-- CAMPO AÑADIDO PARA LEER TUS PROYECTOS
        val ubicacion: List<UbicacionJson>,
        val estados: List<Estado>
    )

    private data class UbicacionJson(
        val id: Int,
        val provincia: String,
        val ciudades: List<CiudadJson>,
        val unidadNegocio: List<UnidadNegocioJson>
    )

    private data class CiudadJson(val id: Int, val nombre: String)
    private data class UnidadNegocioJson(val id: Int, val nombre: String, val ciudad: Int, val agencia: List<AgenciaJson>)
    private data class AgenciaJson(val id: Int, val nombre: String)

    private data class ActividadesJsonData(
        val actividadesPreventivo: List<ActividadConRespuestasJson>,
        val actividadesCorrectivo: List<ActividadConRespuestasJson>,
        val tareasDiagnostico: List<ActividadConRespuestasJson> // <-- AÑADE ESTA LÍNEA
    )

    private data class ActividadConRespuestasJson(
        val id: Int,
        val nombre: String,
        val type: String, // <-- Asegúrate de que esta línea exista
        val posiblesRespuestas: List<RespuestaJson>
    )

    private data class RespuestaJson(
        val id: Int,
        val label: String,
        val value: String
    ) {
        companion object {
            fun toPosibleRespuesta(json: RespuestaJson, actividadId: Int): PosibleRespuesta {
                return PosibleRespuesta(
                    id = json.id,
                    label = json.label,
                    value = json.value,
                    actividadId = actividadId
                )
            }
        }
    }
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