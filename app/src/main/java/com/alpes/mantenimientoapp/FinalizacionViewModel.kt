// Archivo: FinalizacionViewModel.kt
package com.alpes.mantenimientoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la UI para esta pantalla
data class FinalizacionUiState(
    val tecnico: Usuario? = null,
    val cliente: Cliente? = null,
    val responsableCliente: String = "",
    val finalizacionExitosa: Boolean = false,
    val userId: Int = 0 // <-- AÑADIMOS ESTO
)

class FinalizacionViewModel(private val dao: AppDao) : ViewModel() {

    private val _uiState = MutableStateFlow(FinalizacionUiState())
    val uiState = _uiState.asStateFlow()

    // Carga los datos necesarios usando solo el equipoId
    fun loadData(equipoId: String) {
        viewModelScope.launch {
            val equipo = dao.obtenerEquipoPorId(equipoId)
            val tarea = equipo?.let { dao.obtenerTareaPorId(it.tareaId) }

            if (tarea != null) {
                val tecnico = dao.obtenerUsuarioPorId(tarea.usuarioId)
                val cliente = dao.obtenerClientePorId(tarea.clienteId)

                _uiState.update {
                    it.copy(
                        tecnico = tecnico,
                        cliente = cliente,
                        userId = tecnico?.id ?: 0 // <-- AÑADIMOS ESTO
                    )
                }
            }
        }
    }

    // Para actualizar el nombre del responsable desde la UI
    fun onResponsableChanged(nombre: String) {
        _uiState.update { it.copy(responsableCliente = nombre) }
    }

    // Guarda la información final y actualiza el estado del equipo
    fun saveAndFinalize(equipoId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.tecnico != null) {
                val finalizacion = MantenimientoFinal(
                    equipoId = equipoId,
                    responsableCliente = currentState.responsableCliente,
                    tecnicoId = currentState.tecnico.id
                )
                dao.insertarFinalizacion(finalizacion)

                // Actualizamos el estado del equipo a "COMPLETADO" (ID 3)
                dao.updateEquipoStatus(equipoId = equipoId, newStatusId = 3)

                // Indicamos a la UI que la operación fue exitosa
                _uiState.update { it.copy(finalizacionExitosa = true) }
            }
        }
    }
}