// Archivo: ChecklistViewModel.kt (REEMPLAZAR COMPLETO)
package com.alpes.mantenimientoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChecklistItemState(
    val actividad: ActividadConRespuestas,
    val decisionSiNo: Boolean? = null,
    val subRespuestaSeleccionada: PosibleRespuesta? = null,
    val textoOtros: String = "",
    var isChecked: Boolean = false
)

data class ChecklistUiState(
    val items: List<ChecklistItemState> = emptyList(),
    val tipo: String = "", // "preventivo", "correctivo", "diagnostico"
    val observacionMantenimiento: String = "",
    val observacionDiagnostico: String = "",
    val versionFirmwareActual: String = "",
    val versionFirmwareDespues: String = "",
    val mostrarDialogoValidacionDiagnostico: Boolean = false,
    val tareasNoCompletadas: List<String> = emptyList(),
    val validationError: String? = null
)

class ChecklistViewModel(private val dao: AppDao) : ViewModel() {

    private val _uiState = MutableStateFlow(ChecklistUiState())
    val uiState = _uiState.asStateFlow()

    private val _showSaveConfirmation = MutableStateFlow(false)
    val showSaveConfirmation = _showSaveConfirmation.asStateFlow()

    private val _navigateToDiagnostic = MutableSharedFlow<String>()
    val navigateToDiagnostic = _navigateToDiagnostic.asSharedFlow()


    fun loadChecklistData(tipo: String, equipoId: String) {
        viewModelScope.launch {
            val actividades = dao.obtenerActividadesConRespuestas(tipo)
            val resultadosGuardados = dao.getResultadosPorEquipo(equipoId)

            val itemsProcesados = actividades.map { actividad ->
                // --- CAMBIO CLAVE: Usamos dbId para encontrar el resultado ---
                val resultado = resultadosGuardados.find { it.actividadId == actividad.actividad.dbId }

                val subRespuesta = resultado?.respuestaValue?.let { savedValue ->
                    actividad.posiblesRespuestas.find { it.value == savedValue }
                }
                val isCheckedDiagnostico = (tipo == "diagnostico" &&
                        resultado != null &&
                        resultado.respuestaValue == "realizado")

                ChecklistItemState(
                    actividad = actividad,
                    decisionSiNo = when (resultado?.decisionSiNo) {
                        "si" -> true
                        "no" -> false
                        else -> null
                    },
                    subRespuestaSeleccionada = subRespuesta,
                    textoOtros = if (tipo == "correctivo" || resultado?.respuestaValue == "otros") {
                        resultado?.observacion ?: ""
                    } else {
                        ""
                    },
                    isChecked = isCheckedDiagnostico
                )
            }

            val obsGeneralManto = resultadosGuardados.find { it.actividadId == -1 }
            val obsGeneralDiag = resultadosGuardados.find { it.actividadId == -4 }
            val vFirmwareActual = resultadosGuardados.find { it.actividadId == -2 }
            val vFirmwareDespues = resultadosGuardados.find { it.actividadId == -3 }

            _uiState.update {
                it.copy(
                    items = itemsProcesados,
                    tipo = tipo,
                    observacionMantenimiento = obsGeneralManto?.observacion ?: "",
                    observacionDiagnostico = obsGeneralDiag?.observacion ?: "",
                    versionFirmwareActual = vFirmwareActual?.observacion ?: "",
                    versionFirmwareDespues = vFirmwareDespues?.observacion ?: ""
                )
            }
        }
    }

    fun saveChecklist(equipoId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value

            // (Validación de Diagnóstico - sin cambios)
            if (currentState.tipo == "diagnostico") {
                val tareasIncompletas = currentState.items
                    .filter { !it.isChecked && it.subRespuestaSeleccionada == null && it.actividad.posiblesRespuestas.isNotEmpty() }
                    .map { it.actividad.actividad.nombre }

                if (tareasIncompletas.isNotEmpty()) {
                    _uiState.update { it.copy(mostrarDialogoValidacionDiagnostico = true, tareasNoCompletadas = tareasIncompletas) }
                    return@launch
                }
            }

            // (Validación de Preventivo/Correctivo - sin cambios)
            if (currentState.tipo == "preventivo" || currentState.tipo == "correctivo") {
                val tareasSinDecision = currentState.items
                    .filter { it.decisionSiNo == null }
                    .map { it.actividad.actividad.nombre }

                if (tareasSinDecision.isNotEmpty()) {
                    val errorMsg = "Debe completar las siguientes tareas:\n• " + tareasSinDecision.joinToString("\n• ")
                    _uiState.update { it.copy(validationError = errorMsg) }
                    return@launch
                }
                val otrosIncompletos = currentState.items.filter { itemState ->
                    (currentState.tipo == "preventivo" && itemState.subRespuestaSeleccionada?.value == "otros" && itemState.textoOtros.isBlank()) ||
                            (currentState.tipo == "correctivo" && itemState.decisionSiNo != null && itemState.textoOtros.isBlank())
                }.map { it.actividad.actividad.nombre }

                if (otrosIncompletos.isNotEmpty()) {
                    val errorMsg = "Debe especificar la causa o detalle para las siguientes tareas:\n• " + otrosIncompletos.joinToString("\n• ")
                    _uiState.update { it.copy(validationError = errorMsg) }
                    return@launch
                }
            }

            currentState.items.forEach { itemState ->
                when (currentState.tipo) {
                    "diagnostico" -> {
                        if (itemState.isChecked || itemState.subRespuestaSeleccionada != null) {
                            val resultado = MantenimientoResultado(
                                equipoId = equipoId,
                                // --- CAMBIO CLAVE: Usamos dbId ---
                                actividadId = itemState.actividad.actividad.dbId,
                                decisionSiNo = null,
                                respuestaValue = itemState.subRespuestaSeleccionada?.value ?: "realizado",
                                observacion = itemState.subRespuestaSeleccionada?.label ?: "Marcado como completado"
                            )
                            dao.insertarResultado(resultado)
                        }
                    }
                    "correctivo" -> {
                        if (itemState.decisionSiNo != null) {
                            val resultado = MantenimientoResultado(
                                equipoId = equipoId,
                                // --- CAMBIO CLAVE: Usamos dbId ---
                                actividadId = itemState.actividad.actividad.dbId,
                                decisionSiNo = if (itemState.decisionSiNo) "si" else "no",
                                respuestaValue = "otros",
                                observacion = itemState.textoOtros
                            )
                            dao.insertarResultado(resultado)
                        }
                    }
                    "preventivo" -> {
                        if (itemState.decisionSiNo != null) {
                            val esOtro = itemState.subRespuestaSeleccionada?.value == "otros"
                            val resultado = MantenimientoResultado(
                                equipoId = equipoId,
                                // --- CAMBIO CLAVE: Usamos dbId ---
                                actividadId = itemState.actividad.actividad.dbId,
                                decisionSiNo = if (itemState.decisionSiNo) "si" else "no",
                                respuestaValue = if (!esOtro) itemState.subRespuestaSeleccionada?.value else "otros",
                                observacion = if (esOtro) itemState.textoOtros else itemState.subRespuestaSeleccionada?.label ?: ""
                            )
                            dao.insertarResultado(resultado)
                        }
                    }
                }
            } // Fin del forEach

            // (Guardado de Observaciones - sin cambios)
            if ((currentState.tipo == "preventivo" || currentState.tipo == "correctivo") && currentState.observacionMantenimiento.isNotBlank()) {
                val obsResultado = MantenimientoResultado(
                    equipoId = equipoId, actividadId = -1, decisionSiNo = null,
                    respuestaValue = currentState.tipo, observacion = currentState.observacionMantenimiento
                )
                dao.insertarResultado(obsResultado)
            }
            if (currentState.tipo == "diagnostico" && currentState.observacionDiagnostico.isNotBlank()) {
                val obsResultado = MantenimientoResultado(
                    equipoId = equipoId, actividadId = -4, decisionSiNo = null,
                    respuestaValue = currentState.tipo, observacion = currentState.observacionDiagnostico
                )
                dao.insertarResultado(obsResultado)
            }

            // (Guardado de Firmware - sin cambios)
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

            dao.updateEquipoStatus(equipoId = equipoId, newStatusId = 2)

            if (currentState.tipo == "preventivo" || currentState.tipo == "correctivo") {
                _navigateToDiagnostic.emit(equipoId)
            } else {
                _showSaveConfirmation.value = true
            }
        }
    }

    // --- CAMBIO CLAVE: Usamos dbId en todas las funciones de actualización ---
    fun onSiNoDecision(actividadId: Int, decision: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items.map { item ->
                    if (item.actividad.actividad.dbId == actividadId) {
                        item.copy(decisionSiNo = decision, subRespuestaSeleccionada = null, textoOtros = "")
                    } else {
                        item
                    }
                }
            )
        }
    }

    fun onSubRespuestaSelected(actividadId: Int, subRespuesta: PosibleRespuesta) {
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items.map { item ->
                    if (item.actividad.actividad.dbId == actividadId) {
                        item.copy(subRespuestaSeleccionada = subRespuesta)
                    } else {
                        item
                    }
                }
            )
        }
    }

    fun onOtrosTextChanged(actividadId: Int, texto: String) {
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items.map { item ->
                    if (item.actividad.actividad.dbId == actividadId) {
                        item.copy(textoOtros = texto)
                    } else {
                        item
                    }
                }
            )
        }
    }

    fun onDiagnosticoCheckedChange(actividadId: Int, isChecked: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items.map { item ->
                    if (item.actividad.actividad.dbId == actividadId && item.actividad.actividad.tipo == "diagnostico") {
                        item.copy(isChecked = isChecked)
                    } else {
                        item
                    }
                }
            )
        }
    }

    // (El resto de funciones no cambia)
    fun onMantenimientoObservationChanged(texto: String) {
        _uiState.update { it.copy(observacionMantenimiento = texto) }
    }

    fun onDiagnosticoObservationChanged(texto: String) {
        _uiState.update { it.copy(observacionDiagnostico = texto) }
    }

    fun dismissSaveConfirmation() { _showSaveConfirmation.value = false }

    fun dismissDiagnosticValidationDialog() {
        _uiState.update { it.copy(mostrarDialogoValidacionDiagnostico = false, tareasNoCompletadas = emptyList()) }
    }

    fun dismissGenericValidationError() {
        _uiState.update { it.copy(validationError = null) }
    }

    fun onVersionActualChanged(version: String) { _uiState.update { it.copy(versionFirmwareActual = version) } }
    fun onVersionDespuesChanged(version: String) { _uiState.update { it.copy(versionFirmwareDespues = version) } }
}