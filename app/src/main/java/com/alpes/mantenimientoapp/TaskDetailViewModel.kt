// Archivo: TaskDetailViewModel.kt
package com.alpes.mantenimientoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskDetailUiState(
    val equipo: Equipo? = null,
    val clienteSeleccionado: Cliente? = null,
    val proyectoSeleccionado: Proyecto? = null,
    val provinciaSeleccionada: Provincia? = null,
    val ciudadSeleccionada: Ciudad? = null,
    val ciudadSearchText: String = "",
    val unidadNegocioSeleccionada: UnidadNegocio? = null,
    val agenciaSeleccionada: Agencia? = null,
    val allClientes: List<Cliente> = emptyList(),
    val allProvincias: List<Provincia> = emptyList(),
    val allUnidadesNegocio: List<UnidadNegocio> = emptyList(),
    val proyectosFiltrados: List<Proyecto> = emptyList(),
    val ciudadesFiltradas: List<Ciudad> = emptyList(),
    val agenciasFiltradas: List<Agencia> = emptyList()
)

open class TaskDetailViewModel(private val dao: AppDao) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskDetailUiState())
    open val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    allClientes = dao.getAllClientes(),
                    allProvincias = dao.getAllProvincias(),
                    allUnidadesNegocio = dao.getAllUnidadesNegocio().distinctBy { u -> u.nombre }
                )
            }
        }
    }

    open fun loadDataForEquipo(equipoId: String) {
        viewModelScope.launch {
            val equipo = dao.obtenerEquipoPorId(equipoId)
            _uiState.update { it.copy(equipo = equipo) }

            if (equipo != null && equipo.tareaId > 0) {
                val tarea = dao.obtenerTareaPorId(equipo.tareaId)
                if (tarea != null) {
                    val cliente = dao.obtenerClientePorId(tarea.clienteId)
                    onClienteSelected(cliente, dao.obtenerProyectoPorId(tarea.proyectoId))

                    val provincia = dao.obtenerProvinciaPorId(tarea.provinciaId)
                    onProvinciaSelected(provincia, dao.obtenerCiudadPorId(tarea.ciudadId))

                    val unidadNegocio = dao.obtenerUnidadNegocioPorId(tarea.unidadNegocioId)
                    // Llamada corregida
                    onUnidadNegocioSelected(unidadNegocio)
                }
            }
        }
    }

    fun onClienteSelected(cliente: Cliente?, autoSelectProject: Proyecto? = null) {
        cliente?.let { cli ->
            viewModelScope.launch {
                val proyectos = dao.getProyectosByCliente(cli.id)
                _uiState.update { state ->
                    state.copy(
                        clienteSeleccionado = cli,
                        proyectosFiltrados = proyectos,
                        proyectoSeleccionado = autoSelectProject ?: state.proyectoSeleccionado
                    )
                }
            }
        }
    }

    fun onProyectoSelected(proyecto: Proyecto) { _uiState.update { it.copy(proyectoSeleccionado = proyecto) } }

    fun onProvinciaSelected(provincia: Provincia?, autoSelectCity: Ciudad? = null) {
        provincia?.let { prov ->
            viewModelScope.launch {
                val ciudades = dao.obtenerCiudadesPorProvincia(prov.id)
                _uiState.update { state ->
                    state.copy(
                        provinciaSeleccionada = prov,
                        ciudadesFiltradas = ciudades,
                        ciudadSeleccionada = autoSelectCity,
                        agenciasFiltradas = emptyList(),
                        agenciaSeleccionada = null
                    )
                }
                autoSelectCity?.let { onCiudadSelected(it, _uiState.value.agenciaSeleccionada) }
            }
        }
    }

    fun onCiudadSelected(ciudad: Ciudad, autoSelectAgencia: Agencia? = null) {
        viewModelScope.launch {
            val agencias = dao.getAgenciasByCiudad(ciudad.id)
            _uiState.update {
                it.copy(
                    ciudadSeleccionada = ciudad,
                    agenciasFiltradas = agencias,
                    agenciaSeleccionada = autoSelectAgencia
                )
            }
        }
    }

    fun onCiudadSearchTextChanged(text: String) {
        _uiState.update { it.copy(ciudadSearchText = text) }
        val provinciaId = _uiState.value.provinciaSeleccionada?.id
        if (provinciaId != null) {
            viewModelScope.launch {
                val allCiudadesDeProvincia = dao.obtenerCiudadesPorProvincia(provinciaId)
                _uiState.update {
                    it.copy(
                        ciudadesFiltradas = allCiudadesDeProvincia.filter { ciudad ->
                            ciudad.nombre.contains(text, ignoreCase = true)
                        }
                    )
                }
            }
        }
    }

    fun onUnidadNegocioSelected(unidadNegocio: UnidadNegocio?) {
        _uiState.update { it.copy(unidadNegocioSeleccionada = unidadNegocio) }
    }

    fun onAgenciaSelected(agencia: Agencia) { _uiState.update { it.copy(agenciaSeleccionada = agencia) } }
}