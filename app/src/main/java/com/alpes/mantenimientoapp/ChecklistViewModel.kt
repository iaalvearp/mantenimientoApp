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
    val respuestasSeleccionadas: Set<PosibleRespuesta> = emptySet(), // Nombre corregido a plural
    val observacion: String = ""
)

// Estado de la UI para toda la pantalla
data class ChecklistUiState(
    val items: List<ChecklistItemState> = emptyList(),
    val tipo: String = "",
    val observacionGeneral: String = ""
)

class ChecklistViewModel(private val dao: AppDao) : ViewModel() {

    private val _uiState = MutableStateFlow(ChecklistUiState())
    val uiState = _uiState.asStateFlow()

    private val _showSaveConfirmation = MutableStateFlow(false)
    val showSaveConfirmation = _showSaveConfirmation.asStateFlow()

    fun loadChecklistData(tipo: String) {
        viewModelScope.launch {
            val actividades = dao.obtenerActividadesConRespuestas(tipo)
            _uiState.update {
                it.copy(
                    items = actividades.map { actividad ->
                        val defaultResponse = if (tipo != "diagnostico") {
                            actividad.posiblesRespuestas.find { pr -> pr.label.equals("No fue necesario", ignoreCase = true) }
                        } else null
                        ChecklistItemState(
                            actividad = actividad,
                            respuestasSeleccionadas = if(defaultResponse != null) setOf(defaultResponse) else emptySet() // Lógica corregida
                        )
                    },
                    tipo = tipo,
                    observacionGeneral = ""
                )
            }
        }
    }

    // NUEVA LÓGICA DE SELECCIÓN (ahora maneja radio y checkbox)
    fun onResponseSelected(actividadId: Int, respuesta: PosibleRespuesta) { // Ya no necesitamos 'esSeleccionMultiple' aquí
        _uiState.update { currentState ->
            val esMultiple = currentState.items
                .find { it.actividad.actividad.id == actividadId }
                ?.actividad?.actividad?.tipoSeleccion == "multiple_choice"

            currentState.copy(
                items = currentState.items.map { item ->
                    if (item.actividad.actividad.id == actividadId) {
                        val nuevasRespuestas = if (esMultiple) {
                            if (item.respuestasSeleccionadas.contains(respuesta)) item.respuestasSeleccionadas - respuesta
                            else item.respuestasSeleccionadas + respuesta
                        } else setOf(respuesta)
                        item.copy(respuestasSeleccionadas = nuevasRespuestas) // Lógica corregida
                    } else item
                }
            )
        }
    }

    // NUEVO: Para actualizar la observación general
    fun onGeneralObservationChanged(texto: String) {
        _uiState.update { it.copy(observacionGeneral = texto) }
    }

    fun saveChecklist(equipoId: String) {
        viewModelScope.launch {
            // Guardar respuestas de cada item
            _uiState.value.items.forEach { itemState ->
                itemState.respuestasSeleccionadas.forEach { respuesta ->
                    val resultado = MantenimientoResultado(
                        equipoId = equipoId,
                        actividadId = itemState.actividad.actividad.id,
                        respuestaValue = respuesta.value,
                        observacion = itemState.observacion
                    )
                    dao.insertarResultado(resultado)
                }
            }
            // Guardar observación general
            if (_uiState.value.observacionGeneral.isNotBlank()) {
                val obsResultado = MantenimientoResultado(
                    equipoId = equipoId,
                    actividadId = -1, // ID especial para la observación general
                    respuestaValue = _uiState.value.tipo, // Guardamos el tipo de checklist aquí
                    observacion = _uiState.value.observacionGeneral
                )
                dao.insertarResultado(obsResultado)
            }
            _showSaveConfirmation.value = true
        }
    }

    fun dismissSaveConfirmation() {
        _showSaveConfirmation.value = false
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