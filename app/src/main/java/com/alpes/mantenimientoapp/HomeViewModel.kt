// Archivo: HomeViewModel.kt
package com.alpes.mantenimientoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// El estado de la UI ahora también incluye el Rol
data class HomeUiState(
    val usuario: Usuario? = null,
    val rol: Rol? = null, // <-- Propiedad para el rol
    val equipos: List<Equipo> = emptyList()
)

class HomeViewModel(private val dao: AppDao) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadDataForUser(userId: Int) {
        viewModelScope.launch {
            // Buscamos el usuario en la BD.
            val usuario = dao.obtenerUsuarioPorId(userId)

            // --- CORRECCIÓN AQUÍ ---
            // Buscamos el rol usando el rolId del usuario y la variable correcta 'dao'
            val rol = usuario?.rolId?.let { dao.obtenerRolPorId(it) }

            // Buscamos las tareas y equipos del usuario.
            val tareas = dao.obtenerTareasPorUsuario(userId)
            val todosLosEquipos = mutableListOf<Equipo>()
            tareas.forEach { tarea ->
                todosLosEquipos.addAll(dao.obtenerEquiposPorTarea(tarea.id))
            }

            // Actualizamos el estado con toda la información nueva.
            _uiState.update { currentState ->
                currentState.copy(
                    usuario = usuario,
                    rol = rol, // <-- Pasamos el rol encontrado
                    equipos = todosLosEquipos
                )
            }
        }
    }
}