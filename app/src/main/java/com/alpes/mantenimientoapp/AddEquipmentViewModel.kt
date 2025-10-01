// Archivo: AddEquipmentViewModel.kt
package com.alpes.mantenimientoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddEquipmentUiState(
    val numeroSerie: String = "",
    val nombre: String = "",
    val modelo: String = "",
    val caracteristicas: String = "",
    val equiposLocales: List<Equipo> = emptyList(),
    val allModelos: List<String> = emptyList() // Para el dropdown de "Modelo del Equipo (Buscar)"
)

class AddEquipmentViewModel(private val dao: AppDao) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEquipmentUiState())
    val uiState = _uiState.asStateFlow()

    // Esta función carga los modelos existentes y los equipos locales del usuario.
    fun loadInitialData(userId: Int) {
        viewModelScope.launch {
            val allModelos = dao.getUniqueModelos()
            // ¡AQUÍ ESTÁ LA MAGIA! Usamos la función correcta que filtra por usuario
            val equiposLocales = dao.obtenerEquiposLocalesPorUsuario(userId)
            _uiState.update { it.copy(allModelos = allModelos, equiposLocales = equiposLocales) }
        }
    }

    fun onNumeroSerieChanged(valor: String) { _uiState.update { it.copy(numeroSerie = valor) } }

    fun onModeloSearchChanged(valor: String) {
        // Al escribir en el buscador de modelo, limpiamos los campos para que se autorellenen si se selecciona uno.
        _uiState.update { it.copy(modelo = valor, nombre = "", caracteristicas = "") }
    }

    fun onModeloSelected(modelo: String) {
        viewModelScope.launch {
            val detalles = dao.getDetailsForModel(modelo)
            _uiState.update {
                it.copy(
                    modelo = modelo,
                    nombre = detalles?.nombre ?: "",
                    caracteristicas = detalles?.caracteristicas ?: ""
                )
            }
        }
    }

    // Aquí el error que encontraste: para equipos locales, el cliente, provincia, etc. NO DEBEN ser seleccionados
    // por el usuario en este formulario. Asumimos valores predeterminados o los dejamos nulos.
    // Más adelante, si un equipo local se vincula a una tarea (API), entonces la tarea tendrá esos datos.
    fun saveEquipment(userId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            // Para equipos locales, la tareaId será -1 o un valor especial que indique "local"
            val equipoNuevo = Equipo(
                id = currentState.numeroSerie,
                nombre = currentState.nombre,
                modelo = currentState.modelo,
                caracteristicas = currentState.caracteristicas,
                estadoId = 1, // Por defecto, estado "pendiente" para nuevos equipos.
                tareaId = -1, // Importante: -1 indica que es un equipo local sin tarea asignada.
                syncPending = true, // Podría requerir sincronización más adelante.
                esSincronizado = false,
                creadoPorUsuarioId = userId // Se vincula al usuario que lo creó.
            )
            dao.insertarEquipo(equipoNuevo)

            // Refrescamos la lista de equipos locales y limpiamos el formulario.
            loadInitialData(userId)
            _uiState.update {
                it.copy(
                    numeroSerie = "",
                    nombre = "",
                    modelo = "",
                    caracteristicas = ""
                )
            }
        }
    }
}