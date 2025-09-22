// Archivo: FinalizacionViewModel.kt
package com.alpes.mantenimientoapp

import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

// Estado de la UI para esta pantalla
data class FinalizacionUiState(
    val tecnico: Usuario? = null,
    val cliente: Cliente? = null,
    val responsableCliente: String = "",
    val finalizacionExitosa: Boolean = false,
    val userId: Int = 0,
    val fotosPreventivas: List<Uri> = emptyList(), // Uris de las fotos seleccionadas/tomadas
    val fotosCorrectivas: List<Uri> = emptyList() // Uris de las fotos seleccionadas/tomadas
)

// Cambiamos a AndroidViewModel para tener acceso al Context para guardar archivos
class FinalizacionViewModel(private val dao: AppDao, application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FinalizacionUiState())
    val uiState = _uiState.asStateFlow()

    private val applicationContext = application.applicationContext

    fun loadData(equipoId: String) {
        viewModelScope.launch {
            val equipo = dao.obtenerEquipoPorId(equipoId)
            val tarea = equipo?.let { dao.obtenerTareaPorId(it.tareaId) }

            if (tarea != null) {
                val tecnico = dao.obtenerUsuarioPorId(tarea.usuarioId)
                val cliente = dao.obtenerClientePorId(tarea.clienteId)

                // Cargar fotos existentes si las hubiera (para mostrar si el usuario vuelve)
                val fotosPrev = dao.obtenerFotosPorEquipoYTipo(equipoId, "preventivo").map { Uri.parse(it.rutaArchivo) }
                val fotosCorr = dao.obtenerFotosPorEquipoYTipo(equipoId, "correctivo").map { Uri.parse(it.rutaArchivo) }


                _uiState.update {
                    it.copy(
                        tecnico = tecnico,
                        cliente = cliente,
                        userId = tecnico?.id ?: 0,
                        fotosPreventivas = fotosPrev,
                        fotosCorrectivas = fotosCorr
                    )
                }
            }
        }
    }

    fun onResponsableChanged(nombre: String) {
        _uiState.update { it.copy(responsableCliente = nombre) }
    }

    fun addPhoto(uri: Uri, tipoFoto: String) {
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

                // Guardar las fotos en el almacenamiento interno y sus rutas en la BD
                savePhotosToStorage(equipoId, numeroSerie, "preventivo", currentState.fotosPreventivas)
                savePhotosToStorage(equipoId, numeroSerie, "correctivo", currentState.fotosCorrectivas)

                dao.updateEquipoStatus(equipoId = equipoId, newStatusId = 3)

                _uiState.update { it.copy(finalizacionExitosa = true) }
            }
        }
    }

    private suspend fun savePhotosToStorage(
        equipoId: String,
        numeroSerie: String,
        tipoFoto: String,
        photoUris: List<Uri>
    ) = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())

        val appMediaDir = File(applicationContext.getExternalFilesDir(null), "MantenimientoAppMedia")
        val tipoFotoDir = File(appMediaDir, tipoFoto)
        val equipoDir = File(tipoFotoDir, "${numeroSerie}-${timestamp}") // Carpeta [numero de serie]-[fecha]

        if (!equipoDir.exists()) {
            equipoDir.mkdirs() // Crea todas las carpetas necesarias
        }

        photoUris.forEachIndexed { index, uri ->
            try {
                val originalInputStream = applicationContext.contentResolver.openInputStream(uri)
                originalInputStream?.use { input ->
                    val fileName = "${tipoFoto}_${index + 1}.jpg" // Nombra las fotos: preventivo_1.jpg, correctivo_1.jpg
                    val destinationFile = File(equipoDir, fileName)
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }

                    // Guardar la ruta en la base de datos
                    val mantenimientoFoto = MantenimientoFoto(
                        equipoId = equipoId,
                        tipoFoto = tipoFoto,
                        rutaArchivo = destinationFile.absolutePath // Guardamos la ruta absoluta
                    )
                    dao.insertarMantenimientoFoto(mantenimientoFoto)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                // Manejar el error de guardado de la foto
            }
        }
    }
}