// Archivo: FinalizacionViewModel.kt (REEMPLAZAR COMPLETO)
package com.alpes.mantenimientoapp

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FinalizacionUiState(
    val tecnico: Usuario? = null,
    val cliente: Cliente? = null,
    val responsableCliente: String = "",
    val finalizacionExitosa: Boolean = false,
    val userId: Int = 0,
    val fotosPreventivas: List<Uri> = emptyList(),
    val fotosCorrectivas: List<Uri> = emptyList(),

    // --- CAMBIOS PARA REQ #9 y #11 ---
    val isPreventiveCompleted: Boolean = false,
    val isCorrectiveCompleted: Boolean = false,
    val validationError: String? = null
) {
    // --- REQ #10 y #11: Lógica de conteo de fotos ---
    // Calculamos el total de fotos
    private val totalFotos: Int = fotosPreventivas.size + fotosCorrectivas.size

    // El requisito mínimo de 4 fotos se cumple
    val isMinPhotoRequirementMet: Boolean = totalFotos >= 4

    // El requisito máximo de 6 fotos se cumple
    val isMaxPhotoRequirementMet: Boolean = totalFotos < 6
}

// Usamos AndroidViewModel para tener acceso al Contexto de la aplicación
class FinalizacionViewModel(private val dao: AppDao, application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FinalizacionUiState())
    val uiState = _uiState.asStateFlow()

    // Carga los datos iniciales
    fun loadData(equipoId: String) {
        viewModelScope.launch {
            val equipo = dao.obtenerEquipoPorId(equipoId)
            val tarea = equipo?.let { dao.obtenerTareaPorId(it.tareaId) }

            // --- REQ #9: Verificamos qué mantenimientos se hicieron ---
            val preventiveCount = dao.contarResultadosPorTipo(equipoId, "preventivo")
            val correctiveCount = dao.contarResultadosPorTipo(equipoId, "correctivo")

            if (tarea != null) {
                val tecnico = dao.obtenerUsuarioPorId(tarea.usuarioId)
                val cliente = dao.obtenerClientePorId(tarea.clienteId)
                _uiState.update {
                    it.copy(
                        tecnico = tecnico,
                        cliente = cliente,
                        userId = tecnico?.id ?: 0,
                        // Asignamos el estado para que la UI reaccione
                        isPreventiveCompleted = preventiveCount > 0,
                        isCorrectiveCompleted = correctiveCount > 0
                    )
                }
            }
        }
    }

    fun onResponsableChanged(nombre: String) {
        _uiState.update { it.copy(responsableCliente = nombre) }
    }

    // --- REQ #10: Función para añadir foto con validación de MÁXIMO ---
    fun addPhotoUri(uri: Uri, tipoFoto: String) {
        // 1. Verificamos si ya alcanzamos el máximo
        if (!_uiState.value.isMaxPhotoRequirementMet) {
            _uiState.update { it.copy(validationError = "No se pueden agregar más de 6 fotos.") }
            return
        }

        // 2. Si no, agregamos la foto
        _uiState.update { currentState ->
            when (tipoFoto) {
                "preventivo" -> currentState.copy(fotosPreventivas = currentState.fotosPreventivas + uri)
                "correctivo" -> currentState.copy(fotosCorrectivas = currentState.fotosCorrectivas + uri)
                else -> currentState
            }
        }
    }

    // --- REQ #11: Función de guardado con validación de MÍNIMO ---
    fun saveAndFinalize(equipoId: String, numeroSerie: String) {
        viewModelScope.launch {
            val currentState = _uiState.value

            // 1. Validamos el mínimo de fotos
            if (!currentState.isMinPhotoRequirementMet) {
                _uiState.update { it.copy(validationError = "Debe cargar un mínimo de 4 fotos para finalizar.") }
                return@launch
            }

            // 2. Validamos que el responsable esté lleno (aunque el botón lo hace)
            if (currentState.responsableCliente.isBlank()) {
                _uiState.update { it.copy(validationError = "Debe ingresar el nombre del responsable del cliente.") }
                return@launch
            }

            // 3. Si todo está bien, guardamos
            if (currentState.tecnico != null) {
                val finalizacion = MantenimientoFinal(
                    equipoId = equipoId,
                    responsableCliente = currentState.responsableCliente,
                    tecnicoId = currentState.tecnico.id
                )
                dao.insertarFinalizacion(finalizacion)

                savePhotosToStorage("preventivo", equipoId, numeroSerie, currentState.fotosPreventivas)
                savePhotosToStorage("correctivo", equipoId, numeroSerie, currentState.fotosCorrectivas)

                dao.updateEquipoStatus(equipoId = equipoId, newStatusId = 3)
                _uiState.update { it.copy(finalizacionExitosa = true) }
            }
        }
    }

    // Nueva función para descartar el diálogo de error
    fun dismissValidationError() {
        _uiState.update { it.copy(validationError = null) }
    }

    // (La función savePhotosToStorage se mantiene exactamente igual)
    private suspend fun savePhotosToStorage(
        tipoFoto: String,
        equipoId: String,
        numeroSerie: String,
        uris: List<Uri>
    ) = withContext(Dispatchers.IO) {
        val appContext = getApplication<Application>().applicationContext
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val folderName = "${numeroSerie}_${timeStamp}"

        val mainDirectory = File(appContext.getExternalFilesDir(null), "MantenimientoAppMedia")
        val typeDirectory = File(mainDirectory, tipoFoto)
        val sessionDirectory = File(typeDirectory, folderName)
        if (!sessionDirectory.exists()) {
            sessionDirectory.mkdirs()
        }

        uris.forEachIndexed { index, uri ->
            try {
                appContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val file = File(sessionDirectory, "img_${index + 1}.jpg")
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    val foto = MantenimientoFoto(
                        equipoId = equipoId,
                        tipoFoto = tipoFoto,
                        rutaArchivo = file.absolutePath
                    )
                    dao.insertarMantenimientoFoto(foto)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}