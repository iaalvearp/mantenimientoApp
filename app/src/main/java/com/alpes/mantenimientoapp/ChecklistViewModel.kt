package com.alpes.mantenimientoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Representa el estado de un solo item en la checklist
data class ChecklistItemState(
    val actividad: ActividadConRespuestas,
    val respuestaSeleccionada: PosibleRespuesta? = null,
    val observacion: String = ""
)

// Estado de la UI para toda la pantalla
data class ChecklistUiState(
    val items: List<ChecklistItemState> = emptyList(),
    val tipo: String = "preventivo"
)

class ChecklistViewModel(private val dao: AppDao) : ViewModel() {

    private val _uiState = MutableStateFlow(ChecklistUiState())
    val uiState = _uiState.asStateFlow()

    // Controlará la visibilidad del diálogo de confirmación
    private val _showSaveConfirmation = MutableStateFlow(false)
    val showSaveConfirmation = _showSaveConfirmation.asStateFlow()

    fun loadChecklistData(tipo: String) {
        viewModelScope.launch {
            val actividades = dao.obtenerActividadesConRespuestas(tipo)
            _uiState.update {
                it.copy(
                    items = actividades.map { actividad ->
                        // LÓGICA DE SELECCIÓN POR DEFECTO
                        val defaultResponse = actividad.posiblesRespuestas
                            .find { pr -> pr.label.equals("No fue necesario", ignoreCase = true) }

                        ChecklistItemState(
                            actividad = actividad,
                            respuestaSeleccionada = defaultResponse // <-- Asignamos la respuesta por defecto
                        )
                    },
                    tipo = tipo
                )
            }
        }
    }

    // NUEVA FUNCIÓN DE GUARDADO
    fun saveChecklist(equipoId: String) {
        viewModelScope.launch {
            val itemsToSave = _uiState.value.items
            itemsToSave.forEach { itemState ->
                val resultado = MantenimientoResultado(
                    equipoId = equipoId,
                    actividadId = itemState.actividad.actividad.id,
                    respuestaValue = itemState.respuestaSeleccionada?.value,
                    observacion = itemState.observacion
                )
                dao.insertarResultado(resultado)
            }
            // Activamos el diálogo de confirmación
            _showSaveConfirmation.value = true
        }
    }

    fun dismissSaveConfirmation() {
        _showSaveConfirmation.value = false
    }

    fun onResponseSelected(actividadId: Int, respuesta: PosibleRespuesta) {
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items.map { item ->
                    if (item.actividad.actividad.id == actividadId) {
                        item.copy(respuestaSeleccionada = respuesta)
                    } else {
                        item
                    }
                }
            )
        }
    }

    fun onObservationChanged(actividadId: Int, texto: String) {
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items.map { item ->
                    if (item.actividad.actividad.id == actividadId) {
                        item.copy(observacion = texto)
                    } else {
                        item
                    }
                }
            )
        }
    }
}