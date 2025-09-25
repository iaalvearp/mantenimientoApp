// Archivo: AddEquipmentViewModel.kt
package com.alpes.mantenimientoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// El estado vuelve a ser más simple, sin el modo edición
data class AddEquipmentUiState(
    val numeroSerie: String = "",
    val nombre: String = "",
    val modelo: String = "",
    val caracteristicas: String = "",
    val equiposLocales: List<Equipo> = emptyList()
)

class AddEquipmentViewModel(private val dao: AppDao) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEquipmentUiState())
    val uiState = _uiState.asStateFlow()

    fun onNumeroSerieChanged(valor: String) { _uiState.update { it.copy(numeroSerie = valor) } }
    fun onNombreChanged(valor: String) { _uiState.update { it.copy(nombre = valor) } }
    fun onModeloChanged(valor: String) { _uiState.update { it.copy(modelo = valor) } }
    fun onCaracteristicasChanged(valor: String) { _uiState.update { it.copy(caracteristicas = valor) } }

    fun loadLocalEquipment() {
        viewModelScope.launch {
            val equipos = dao.obtenerEquiposPorTarea(-1)
            _uiState.update { it.copy(equiposLocales = equipos) }
        }
    }

    fun saveEquipment() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val equipoNuevo = Equipo(
                id = currentState.numeroSerie,
                nombre = currentState.nombre,
                modelo = currentState.modelo,
                caracteristicas = currentState.caracteristicas,
                estadoId = 1,
                tareaId = -1,
                syncPending = true,
                esSincronizado = false
            )
            dao.insertarEquipo(equipoNuevo)

            // Refrescamos la lista y limpiamos el formulario en una sola acción
            val equiposActualizados = dao.obtenerEquiposPorTarea(-1)
            _uiState.update {
                it.copy(
                    equiposLocales = equiposActualizados,
                    numeroSerie = "",
                    nombre = "",
                    modelo = "",
                    caracteristicas = ""
                )
            }
        }
    }
}