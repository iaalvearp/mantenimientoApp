// Archivo: ChecklistViewModel.kt (COMPLETAMENTE ACTUALIZADO)
package com.alpes.mantenimientoapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- CAMBIO: La nueva estructura para el estado de CADA item del checklist ---
data class ChecklistItemState(
    val actividad: ActividadConRespuestas,
    // Guarda la decisión inicial del técnico: null (sin responder), true (Sí), false (No)
    val decisionSiNo: Boolean? = null,
    // Guarda la sub-respuesta detallada que el técnico elige
    val subRespuestaSeleccionada: PosibleRespuesta? = null,
    // Guarda el texto personalizado si la respuesta es "Otros"
    val textoOtros: String = ""
)

// --- CAMBIO: El estado general de la UI ahora usa la nueva estructura de items ---
data class ChecklistUiState(
    val items: List<ChecklistItemState> = emptyList(),
    val tipo: String = "",
    val observacionGeneral: String = "",
    val versionFirmwareActual: String = "",
    val versionFirmwareDespues: String = ""
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
                    // Al cargar, simplemente mapeamos las actividades a nuestro nuevo estado inicial
                    items = actividades.map { actividad -> ChecklistItemState(actividad = actividad) },
                    tipo = tipo,
                    // Reseteamos los demás campos
                    observacionGeneral = "",
                    versionFirmwareActual = "",
                    versionFirmwareDespues = ""
                )
            }
        }
    }

    // --- NUEVA FUNCIÓN: Se activa cuando el usuario presiona "Sí" o "No" ---
    fun onSiNoDecision(actividadId: Int, decision: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items.map { item ->
                    if (item.actividad.actividad.id == actividadId) {
                        // Actualizamos la decisión y reseteamos cualquier sub-respuesta anterior
                        item.copy(decisionSiNo = decision, subRespuestaSeleccionada = null, textoOtros = "")
                    } else {
                        item
                    }
                }
            )
        }
    }

    // --- NUEVA FUNCIÓN: Se activa cuando el usuario elige una sub-respuesta de la lista ---
    fun onSubRespuestaSelected(actividadId: Int, subRespuesta: PosibleRespuesta) {
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items.map { item ->
                    if (item.actividad.actividad.id == actividadId) {
                        item.copy(subRespuestaSeleccionada = subRespuesta)
                    } else {
                        item
                    }
                }
            )
        }
    }

    // --- NUEVA FUNCIÓN: Se activa cuando el usuario escribe en el campo "Otros" ---
    fun onOtrosTextChanged(actividadId: Int, texto: String) {
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items.map { item ->
                    if (item.actividad.actividad.id == actividadId) {
                        item.copy(textoOtros = texto)
                    } else {
                        item
                    }
                }
            )
        }
    }

    fun onGeneralObservationChanged(texto: String) {
        _uiState.update { it.copy(observacionGeneral = texto) }
    }

    // --- LÓGICA DE GUARDADO COMPLETAMENTE REESCRITA ---
    fun saveChecklist(equipoId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value

            currentState.items.forEach { itemState ->
                // Solo guardamos algo si el usuario ha tomado al menos la decisión de Sí/No
                if (itemState.decisionSiNo != null) {
                    val esOtro = itemState.subRespuestaSeleccionada?.value == "otros"

                    val resultado = MantenimientoResultado(
                        equipoId = equipoId,
                        actividadId = itemState.actividad.actividad.id,
                        // Guardamos "si" o "no" para tu futuro formulario
                        decisionSiNo = if (itemState.decisionSiNo) "si" else "no",
                        // Si es "Otros", guardamos null aquí para no guardar "otros" como valor
                        respuestaValue = if (!esOtro) itemState.subRespuestaSeleccionada?.value else null,
                        // Si es "Otros", la observación es el texto del usuario. Si no, es la etiqueta de la opción.
                        observacion = if (esOtro) itemState.textoOtros else itemState.subRespuestaSeleccionada?.label ?: ""
                    )
                    dao.insertarResultado(resultado)
                }
            }

            // Lógica para guardar la observación general y firmware (sin cambios)
            if (currentState.observacionGeneral.isNotBlank()) {
                val obsResultado = MantenimientoResultado(
                    equipoId = equipoId, actividadId = -1, decisionSiNo = null,
                    respuestaValue = currentState.tipo, observacion = currentState.observacionGeneral
                )
                dao.insertarResultado(obsResultado)
            }
            if (currentState.versionFirmwareActual.isNotBlank()) {
                val firmwareActualResultado = MantenimientoResultado(
                    equipoId = equipoId, actividadId = -2, decisionSiNo = null,
                    respuestaValue = currentState.tipo, observacion = currentState.versionFirmwareActual
                )
                dao.insertarResultado(firmwareActualResultado)
            }
            if (currentState.versionFirmwareDespues.isNotBlank()) {
                val firmwareDespuesResultado = MantenimientoResultado(
                    equipoId = equipoId, actividadId = -3, decisionSiNo = null,
                    respuestaValue = currentState.tipo, observacion = currentState.versionFirmwareDespues
                )
                dao.insertarResultado(firmwareDespuesResultado)
            }

            // Actualizamos el estado del equipo a "en progreso"
            dao.updateEquipoStatus(equipoId = equipoId, newStatusId = 2)
            _showSaveConfirmation.value = true
        }
    }

    fun dismissSaveConfirmation() {
        _showSaveConfirmation.value = false
    }

    // Funciones de versión de firmware (sin cambios)
    fun onVersionActualChanged(version: String) { _uiState.update { it.copy(versionFirmwareActual = version) } }
    fun onVersionDespuesChanged(version: String) { _uiState.update { it.copy(versionFirmwareDespues = version) } }

    // Estas funciones ya no se usan para el nuevo flujo, pero las mantenemos por si Diagnóstico las necesita
    fun onResponseSelected(actividadId: Int, respuesta: PosibleRespuesta) {
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items.map { item ->
                    if (item.actividad.actividad.id == actividadId) {
                        item.copy(subRespuestaSeleccionada = respuesta)
                    } else item
                }
            )
        }
    }
    fun onObservationChanged(actividadId: Int, texto: String) {
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items.map { item ->
                    if (item.actividad.actividad.id == actividadId) {
                        item.copy(textoOtros = texto)
                    } else item
                }
            )
        }
    }
}