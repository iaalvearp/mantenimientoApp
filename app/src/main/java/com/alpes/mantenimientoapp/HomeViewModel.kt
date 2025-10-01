// Archivo: HomeViewModel.kt
package com.alpes.mantenimientoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val usuario: Usuario? = null,
    val rol: Rol? = null,
    val equipos: List<Equipo> = emptyList()
)

class HomeViewModel(private val dao: AppDao) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadDataForUser(userId: Int) {
        viewModelScope.launch {
            val usuario = dao.obtenerUsuarioPorId(userId)
            val rol = usuario?.rolId?.let { dao.obtenerRolPorId(it) }

            // Lógica de carga final y correcta
            val tareasAsignadas = dao.obtenerTareasPorUsuario(userId)
            val idsDeTareas = tareasAsignadas.map { it.id }

            val equiposDeTareas = if (idsDeTareas.isNotEmpty()) {
                dao.obtenerEquiposPorTareas(idsDeTareas)
            } else { emptyList() }

            _uiState.update {
                it.copy(
                    usuario = usuario,
                    rol = rol,
                    equipos = equiposDeTareas
                )
            }
        }
    }
}