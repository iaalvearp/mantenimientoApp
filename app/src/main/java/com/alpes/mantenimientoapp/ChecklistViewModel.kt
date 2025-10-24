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
    val tipo: String = "",
    val observacionGeneral: String = "",
    val versionFirmwareActual: String = "",
    val versionFirmwareDespues: String = "",
    val mostrarDialogoValidacion: Boolean = false,
    val tareasNoCompletadas: List<String> = emptyList()
)

class ChecklistViewModel(private val dao: AppDao) : ViewModel() {

    private val _uiState = MutableStateFlow(ChecklistUiState())
    val uiState = _uiState.asStateFlow()

    private val _showSaveConfirmation = MutableStateFlow(false)
    val showSaveConfirmation = _showSaveConfirmation.asStateFlow()

    // --- CAMBIO #1: Creamos una "señal" para la navegación ---
    // Usamos SharedFlow porque es para eventos de una sola vez (como navegar).
    private val _navigateToDiagnostic = MutableSharedFlow<String>()
    val navigateToDiagnostic = _navigateToDiagnostic.asSharedFlow()


    fun loadChecklistData(tipo: String) {
        viewModelScope.launch {
            val actividades = dao.obtenerActividadesConRespuestas(tipo)

            // --- CAMBIO #2: Lógica para simplificar el Mantenimiento Correctivo ---
            val itemsProcesados = if (tipo == "correctivo") {
                actividades.map { actividadConRespuestas ->
                    // Creamos una única respuesta posible para "Ingresar causa..."
                    val respuestaUnica = PosibleRespuesta(
                        dbId = -1, // ID temporal, no se guarda
                        id = -1,
                        label = "Ingresar causa...", // El nuevo texto
                        value = "otros", // El valor que activa el campo de texto
                        actividadId = actividadConRespuestas.actividad.dbId,
                        esParaRespuestaAfirmativa = false // O true, según tu lógica de negocio
                    )
                    // Reemplazamos las posibles respuestas originales por nuestra lista con un solo item
                    val actividadModificada = actividadConRespuestas.copy(
                        posiblesRespuestas = listOf(respuestaUnica)
                    )
                    ChecklistItemState(actividad = actividadModificada)
                }
            } else {
                actividades.map { actividad -> ChecklistItemState(actividad = actividad) }
            }

            _uiState.update {
                it.copy(
                    items = itemsProcesados, // Usamos los items ya procesados
                    tipo = tipo,
                    observacionGeneral = "",
                    versionFirmwareActual = "",
                    versionFirmwareDespues = ""
                )
            }
        }
    }

    fun saveChecklist(equipoId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value

            if (currentState.tipo == "diagnostico") {
                val tareasIncompletas = currentState.items
                    .filter { !it.isChecked && it.subRespuestaSeleccionada == null && it.actividad.posiblesRespuestas.isNotEmpty() }
                    .map { it.actividad.actividad.nombre }

                if (tareasIncompletas.isNotEmpty()) {
                    _uiState.update { it.copy(mostrarDialogoValidacion = true, tareasNoCompletadas = tareasIncompletas) }
                    return@launch
                }
            }

            currentState.items.forEach { itemState ->
                if (currentState.tipo == "diagnostico") {
                    if (itemState.isChecked || itemState.subRespuestaSeleccionada != null) {
                        val resultado = MantenimientoResultado(
                            equipoId = equipoId,
                            actividadId = itemState.actividad.actividad.id,
                            decisionSiNo = null,
                            respuestaValue = itemState.subRespuestaSeleccionada?.value ?: "realizado",
                            observacion = itemState.subRespuestaSeleccionada?.label ?: "Marcado como completado"
                        )
                        dao.insertarResultado(resultado)
                    }
                } else {
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

            dao.updateEquipoStatus(equipoId = equipoId, newStatusId = 2)

            // --- CAMBIO #3: Emitir la señal de navegación si no es diagnóstico ---
            if (currentState.tipo == "preventivo" || currentState.tipo == "correctivo") {
                _navigateToDiagnostic.emit(equipoId)
            } else {
                // Si es diagnóstico, solo mostramos el diálogo de confirmación como antes
                _showSaveConfirmation.value = true
            }
        }
    }

    // --- El resto del archivo no tiene cambios, lo incluyo para que sea completo ---

    fun onSiNoDecision(actividadId: Int, decision: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items.map { item ->
                    if (item.actividad.actividad.id == actividadId) {
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
                    if (item.actividad.actividad.id == actividadId) {
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
                    if (item.actividad.actividad.id == actividadId) {
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

    fun dismissSaveConfirmation() { _showSaveConfirmation.value = false }

    fun dismissValidationDialog() {
        _uiState.update { it.copy(mostrarDialogoValidacion = false, tareasNoCompletadas = emptyList()) }
    }

    fun onVersionActualChanged(version: String) { _uiState.update { it.copy(versionFirmwareActual = version) } }
    fun onVersionDespuesChanged(version: String) { _uiState.update { it.copy(versionFirmwareDespues = version) } }

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