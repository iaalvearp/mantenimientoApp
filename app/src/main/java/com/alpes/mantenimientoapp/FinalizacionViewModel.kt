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
    // --- NUEVO: Listas para las URIs de las fotos ---
    val fotosPreventivas: List<Uri> = emptyList(),
    val fotosCorrectivas: List<Uri> = emptyList()
)

// Usamos AndroidViewModel para tener acceso al Contexto de la aplicación
class FinalizacionViewModel(private val dao: AppDao, application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FinalizacionUiState())
    val uiState = _uiState.asStateFlow()

    // Carga los datos iniciales (igual que antes)
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
                        userId = tecnico?.id ?: 0
                    )
                }
            }
        }
    }

    fun onResponsableChanged(nombre: String) {
        _uiState.update { it.copy(responsableCliente = nombre) }
    }

    // --- NUEVO: Función para añadir una foto a la lista correspondiente ---
    fun addPhotoUri(uri: Uri, tipoFoto: String) {
        _uiState.update { currentState ->
            when (tipoFoto) {
                "preventivo" -> currentState.copy(fotosPreventivas = currentState.fotosPreventivas + uri)
                "correctivo" -> currentState.copy(fotosCorrectivas = currentState.fotosCorrectivas + uri)
                else -> currentState
            }
        }
    }

    fun saveAndFinalize(equipoId: String, numeroSerie: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.tecnico != null) {
                val finalizacion = MantenimientoFinal(
                    equipoId = equipoId,
                    responsableCliente = currentState.responsableCliente,
                    tecnicoId = currentState.tecnico.id
                )
                dao.insertarFinalizacion(finalizacion)

                // --- NUEVO: Lógica para guardar las fotos ---
                savePhotosToStorage("preventivo", equipoId, numeroSerie, currentState.fotosPreventivas)
                savePhotosToStorage("correctivo", equipoId, numeroSerie, currentState.fotosCorrectivas)

                dao.updateEquipoStatus(equipoId = equipoId, newStatusId = 3)
                _uiState.update { it.copy(finalizacionExitosa = true) }
            }
        }
    }

    // --- NUEVO: Función privada para manejar el guardado de archivos ---
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
                    // Guardamos la ruta en la base de datos
                    val foto = MantenimientoFoto(
                        equipoId = equipoId,
                        tipoFoto = tipoFoto,
                        rutaArchivo = file.absolutePath
                    )
                    dao.insertarMantenimientoFoto(foto)
                }
            } catch (e: IOException) {
                e.printStackTrace() // Manejo de errores
            }
        }
    }
}