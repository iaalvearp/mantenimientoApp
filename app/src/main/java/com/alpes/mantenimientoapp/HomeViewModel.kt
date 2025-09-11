// Archivo: HomeViewModel.kt
package com.alpes.mantenimientoapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

// KOTLIN: Creamos una clase de datos para contener toda la información de la pantalla.
data class HomeUiState(
    val usuario: Usuario? = null,
    val equipos: List<Equipo> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val appDao = AppDatabase.getDatabase(application).appDao()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadDataForUser(userId: Int) {
        viewModelScope.launch {
            // Buscamos el usuario en la BD.
            val usuario = appDao.obtenerUsuarioPorId(userId) // Necesitaremos añadir esta función al DAO.

            // Buscamos las tareas y equipos del usuario.
            val tareas = appDao.obtenerTareasPorUsuario(userId)
            val todosLosEquipos = mutableListOf<Equipo>()
            tareas.forEach { tarea ->
                todosLosEquipos.addAll(appDao.obtenerEquiposPorTarea(tarea.id))
            }

            // Actualizamos el estado con toda la información nueva.
            _uiState.update { currentState ->
                currentState.copy(
                    usuario = usuario,
                    equipos = todosLosEquipos
                )
            }
        }
    }
}