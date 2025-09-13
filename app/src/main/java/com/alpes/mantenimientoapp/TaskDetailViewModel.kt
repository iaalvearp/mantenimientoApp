package com.alpes.mantenimientoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado que representa la información de la pantalla de detalle
data class TaskDetailUiState(
    val equipo: Equipo? = null,
    val tarea: Tarea? = null
    // Aquí podríamos añadir más datos, como el nombre del cliente, etc.
)

class TaskDetailViewModel(private val dao: AppDao) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadDataForEquipo(equipoId: String) {
        viewModelScope.launch {
            val equipo = dao.obtenerEquipoPorId(equipoId)
            if (equipo != null) {
                val tarea = dao.obtenerTareaPorId(equipo.tareaId)
                _uiState.update { it.copy(equipo = equipo, tarea = tarea) }
            }
        }
    }
}