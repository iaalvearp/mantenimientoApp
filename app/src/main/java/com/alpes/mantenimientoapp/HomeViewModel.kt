// Archivo: HomeViewModel.kt
package com.alpes.mantenimientoapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val appDao = AppDatabase.getDatabase(application).appDao()

    private val _uiState = MutableStateFlow<List<Equipo>>(emptyList())
    val uiState: StateFlow<List<Equipo>> = _uiState.asStateFlow()

    init {
        // TODO: Más adelante, aquí deberíamos pasar el ID del usuario que hizo login.
        cargarEquiposDelUsuario(101)
    }

    private fun cargarEquiposDelUsuario(usuarioId: Int) {
        viewModelScope.launch {
            val tareas = appDao.obtenerTareasPorUsuario(usuarioId)
            val todosLosEquipos = mutableListOf<Equipo>()

            tareas.forEach { tarea ->
                val equiposDeLaTarea = appDao.obtenerEquiposPorTarea(tarea.id)
                todosLosEquipos.addAll(equiposDeLaTarea)
            }

            _uiState.value = todosLosEquipos
        }
    }
}