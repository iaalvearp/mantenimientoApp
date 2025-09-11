package com.alpes.mantenimientoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alpes.mantenimientoapp.ui.theme.MantenimientoAppTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import androidx.core.content.edit

class MainActivity : ComponentActivity() {
    private lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dao = AppDatabase.getDatabase(applicationContext).appDao()
        viewModelFactory = ViewModelFactory(dao)

        lifecycleScope.launch(Dispatchers.IO) {
            prepopulateDatabaseIfNeeded()
        }

        // CORRECCIÓN 1: Se eliminó el setContent duplicado
        setContent {
            MantenimientoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF33A8FF) // Un color de fondo para distinguirlo
                ) {
                    // Obtenemos una instancia del ViewModel usando nuestra Factory
                    val loginViewModel: LoginViewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]

                    LoginScreen(
                        loginViewModel = loginViewModel,
                        onLoginSuccess = { userId ->
                            // Por ahora, solo imprimimos en consola, pero ya está listo para navegar.
                            println("LOGIN EXITOSO para el usuario ID: $userId")
                        }
                    )
                }
            }
        }
    }

    // CORRECCIÓN 2: La función y las data class ahora están DENTRO de MainActivity
    private suspend fun prepopulateDatabaseIfNeeded() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("is_first_run", true)

        if (isFirstRun) {
            val database = AppDatabase.getDatabase(applicationContext)
            val dao = database.appDao()
            val gson = Gson()

            // --- Cargar Tareas y Equipos ---
            val tareasStream = assets.open("tareas.json")
            val tareaListType = object : TypeToken<List<TareaConEquipos>>() {}.type
            val tareasConEquipos: List<TareaConEquipos> = gson.fromJson(InputStreamReader(tareasStream), tareaListType)

            tareasConEquipos.forEach { tareaConEquipos ->
                dao.insertarTarea(tareaConEquipos.toTarea())
                tareaConEquipos.equipos.forEach { equipoJson ->
                    dao.insertarEquipo(equipoJson.toEquipo(tareaConEquipos.id))
                }
            }

            // --- Cargar Usuarios y Estados desde database.json ---
            val databaseStream = assets.open("database.json")
            val databaseJsonData: DatabaseJsonData = gson.fromJson(InputStreamReader(databaseStream), DatabaseJsonData::class.java)

            databaseJsonData.usuarios.forEach { dao.insertarUsuario(it) }
            databaseJsonData.estados.forEach { dao.insertarEstado(it) }

            prefs.edit { putBoolean("is_first_run", false) }
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
} // <-- FIN DE LA CLASE MainActivity