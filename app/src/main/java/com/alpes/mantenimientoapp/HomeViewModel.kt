// Archivo: HomeViewModel.kt
package com.alpes.mantenimientoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val usuario: Usuario? = null,
    val equipos: List<Equipo> = emptyList()
)

// CORRECCIÓN: Ahora recibe el DAO directamente, igual que los otros ViewModels.
class HomeViewModel(private val dao: AppDao) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadDataForUser(userId: Int) {
        viewModelScope.launch {
            val usuario = dao.obtenerUsuarioPorId(userId)
            val tareas = dao.obtenerTareasPorUsuario(userId)
            val todosLosEquipos = mutableListOf<Equipo>()
            tareas.forEach { tarea ->
                todosLosEquipos.addAll(dao.obtenerEquiposPorTarea(tarea.id))
            }

            _uiState.update { currentState ->
                currentState.copy(
                    usuario = usuario,
                    equipos = todosLosEquipos
                )
            }
        }
    }
}