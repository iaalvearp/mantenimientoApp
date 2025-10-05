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
    // Para Preventivo/Correctivo
    val decisionSiNo: Boolean? = null,
    val subRespuestaSeleccionada: PosibleRespuesta? = null,
    val textoOtros: String = "",
    // --- DIAGNÓSTICO: Nuevo campo para el checkbox ---
    var isChecked: Boolean = false
)

// --- CAMBIO: El estado general de la UI ahora usa la nueva estructura de items ---
data class ChecklistUiState(
    val items: List<ChecklistItemState> = emptyList(),
    val tipo: String = "",
    val observacionGeneral: String = "",
    val versionFirmwareActual: String = "",
    val versionFirmwareDespues: String = "",
    // --- DIAGNÓSTICO: Nuevos estados para el diálogo de validación ---
    val mostrarDialogoValidacion: Boolean = false,
    val tareasNoCompletadas: List<String> = emptyList()
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
                    items = actividades.map { actividad -> ChecklistItemState(actividad = actividad) },
                    tipo = tipo,
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

    // --- DIAGNÓSTICO: Nueva función para manejar el cambio del Checkbox ---
    fun onDiagnosticoCheckedChange(actividadId: Int, isChecked: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items.map { item ->
                    if (item.actividad.actividad.id == actividadId && item.actividad.actividad.tipo == "diagnostico") {
                        item.copy(isChecked = isChecked)
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

    // --- LÓGICA DE GUARDADO ACTUALIZADA CON VALIDACIÓN ---
    fun saveChecklist(equipoId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value

            // --- INICIO DEL NUEVO BLOQUE DE DEPURACIÓN ---
            Log.d("DEBUG_VALIDACION", "--- INICIANDO PROCESO DE GUARDADO ---")
            Log.d("DEBUG_VALIDACION", "Tipo de Checklist: ${currentState.tipo}")
            currentState.items.forEachIndexed { index, item ->
                Log.d("DEBUG_VALIDACION", "Item #${index + 1} (ID: ${item.actividad.actividad.id}): isChecked = ${item.isChecked}")
            }
            Log.d("DEBUG_VALIDACION", "------------------------------------")
            // --- FIN DEL NUEVO BLOQUE DE DEPURACIÓN ---


            // --- DIAGNÓSTICO: Bloque de validación ---
            if (currentState.tipo == "diagnostico") {
                val tareasIncompletas = currentState.items
                    .filter { !it.isChecked && it.actividad.posiblesRespuestas.isNotEmpty() }
                    .map { it.actividad.actividad.nombre }

                if (tareasIncompletas.isNotEmpty()) {
                    _uiState.update { it.copy(mostrarDialogoValidacion = true, tareasNoCompletadas = tareasIncompletas) }
                    return@launch
                }
            }

            // Si la validación pasa (o no es diagnóstico), procedemos a guardar
            currentState.items.forEach { itemState ->
                if (currentState.tipo == "diagnostico") {
                    if (itemState.isChecked) {
                        val resultado = MantenimientoResultado(
                            equipoId = equipoId,
                            actividadId = itemState.actividad.actividad.id,
                            decisionSiNo = null,
                            respuestaValue = "realizado", // Guardamos un valor estándar
                            observacion = "Marcado como completado"
                        )
                        dao.insertarResultado(resultado)
                    }
                } else { // Lógica de Preventivo/Correctivo
                    if (itemState.decisionSiNo != null) {
                        val esOtro = itemState.subRespuestaSeleccionada?.value == "otros"
                        val resultado = MantenimientoResultado(
                            equipoId = equipoId,
                            actividadId = itemState.actividad.actividad.id,
                            decisionSiNo = if (itemState.decisionSiNo) "si" else "no",
                            respuestaValue = if (!esOtro) itemState.subRespuestaSeleccionada?.value else null,
                            observacion = if (esOtro) itemState.textoOtros else itemState.subRespuestaSeleccionada?.label ?: ""
                        )
                        dao.insertarResultado(resultado)
                    }
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

    fun dismissSaveConfirmation() { _showSaveConfirmation.value = false }

    // --- DIAGNÓSTICO: Nueva función para cerrar el diálogo de validación ---
    fun dismissValidationDialog() {
        _uiState.update { it.copy(mostrarDialogoValidacion = false, tareasNoCompletadas = emptyList()) }
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