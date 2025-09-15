package com.alpes.mantenimientoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la UI para la pantalla de checklist
data class ChecklistUiState(
    val actividades: List<ActividadMantenimiento> = emptyList(),
    val respuestas: List<PosibleRespuesta> = emptyList()
)

open class ChecklistViewModel(private val dao: AppDao) : ViewModel() {

    private val _uiState = MutableStateFlow(ChecklistUiState())
    open val uiState = _uiState.asStateFlow()

    init {
        loadActivities()
    }

    open fun loadActivities() {
        viewModelScope.launch {
            val actividades = dao.obtenerActividadesMantenimiento()
            val respuestas = dao.obtenerPosiblesRespuestas()
            _uiState.update {
                it.copy(actividades = actividades, respuestas = respuestas)
            }
        }
    }
}