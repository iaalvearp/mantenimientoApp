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
    val unidadNegocioSeleccionada: UnidadNegocio? = null, // Se mantiene
    val agenciaSeleccionada: Agencia? = null,
    val allClientes: List<Cliente> = emptyList(),
    val allProvincias: List<Provincia> = emptyList(),
    val proyectosFiltrados: List<Proyecto> = emptyList(),
    val ciudadesFiltradas: List<Ciudad> = emptyList(),
    // --- NUEVO: Lista para las unidades de negocio filtradas ---
    val unidadesNegocioFiltradas: List<UnidadNegocio> = emptyList(),
    val agenciasFiltradas: List<Agencia> = emptyList(),
    val agenciaSearchText: String = ""
)

open class TaskDetailViewModel(private val dao: AppDao) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskDetailUiState())
    open val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(allClientes = dao.getAllClientes(), allProvincias = dao.getAllProvincias()) }
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
                    val provincia = dao.obtenerProvinciaPorId(tarea.provinciaId)
                    val ciudad = dao.obtenerCiudadPorId(tarea.ciudadId)
                    // --- LÓGICA DE PRECARGA MODIFICADA ---
                    val unidadNegocio = dao.obtenerUnidadNegocioPorId(tarea.unidadNegocioId)
                    val agencia = dao.obtenerAgenciaPorId(tarea.agenciaId)

                    onClienteSelected(cliente, dao.obtenerProyectoPorId(tarea.proyectoId))
                    // La precarga ahora es más inteligente y va en orden
                    onProvinciaSelected(provincia, autoSelectCity = ciudad, autoSelectUnidad = unidadNegocio, autoSelectAgencia = agencia)
                }
            }
        }
    }

    fun onClienteSelected(cliente: Cliente?, autoSelectProject: Proyecto? = null) {
        _uiState.update { it.copy(clienteSeleccionado = cliente, proyectoSeleccionado = autoSelectProject, proyectosFiltrados = emptyList()) }
        cliente?.let { viewModelScope.launch { _uiState.update { it.copy(proyectosFiltrados = dao.getProyectosByCliente(cliente.id)) } } }
    }
    fun onProyectoSelected(proyecto: Proyecto) { _uiState.update { it.copy(proyectoSeleccionado = proyecto) } }

    // --- FUNCIÓN MODIFICADA: Ahora puede recibir la unidad de negocio y agencia a precargar ---
    fun onProvinciaSelected(provincia: Provincia?, autoSelectCity: Ciudad? = null, autoSelectUnidad: UnidadNegocio? = null, autoSelectAgencia: Agencia? = null) {
        _uiState.update { it.copy(
            provinciaSeleccionada = provincia,
            ciudadSeleccionada = null,
            unidadNegocioSeleccionada = null,
            agenciaSeleccionada = null,
            ciudadesFiltradas = emptyList(),
            unidadesNegocioFiltradas = emptyList(),
            agenciasFiltradas = emptyList(),
            agenciaSearchText = ""
        )}
        provincia?.let {
            viewModelScope.launch {
                _uiState.update { it.copy(ciudadesFiltradas = dao.obtenerCiudadesPorProvincia(provincia.id)) }
                // Si hay una ciudad para precargar, la seleccionamos
                autoSelectCity?.let { onCiudadSelected(it, autoSelectUnidad, autoSelectAgencia) }
            }
        }
    }

    // --- FUNCIÓN MODIFICADA: Ahora carga las Unidades de Negocio ---
    fun onCiudadSelected(ciudad: Ciudad, autoSelectUnidad: UnidadNegocio? = null, autoSelectAgencia: Agencia? = null) {
        _uiState.update { it.copy(
            ciudadSeleccionada = ciudad,
            unidadNegocioSeleccionada = null,
            agenciaSeleccionada = null,
            unidadesNegocioFiltradas = emptyList(),
            agenciasFiltradas = emptyList(),
            agenciaSearchText = ""
        )}
        viewModelScope.launch {
            _uiState.update { it.copy(unidadesNegocioFiltradas = dao.getUnidadesNegocioByCiudad(ciudad.id)) }
            // Si hay una unidad para precargar, la seleccionamos
            autoSelectUnidad?.let { onUnidadNegocioSelected(it, autoSelectAgencia) }
        }
    }

    // --- NUEVA FUNCIÓN: Para manejar la selección de Unidad de Negocio ---
    fun onUnidadNegocioSelected(unidadNegocio: UnidadNegocio, autoSelectAgencia: Agencia? = null) {
        _uiState.update { it.copy(
            unidadNegocioSeleccionada = unidadNegocio,
            agenciaSeleccionada = null,
            agenciasFiltradas = emptyList(),
            agenciaSearchText = ""
        )}
        // Filtramos las agencias que pertenecen a la ciudad Y a la unidad de negocio seleccionadas
        _uiState.value.ciudadSeleccionada?.let { ciudad ->
            viewModelScope.launch {
                val agencias = dao.getAgenciasByCiudad(ciudad.id).filter { it.unidadNegocioId == unidadNegocio.id }
                _uiState.update { it.copy(agenciasFiltradas = agencias) }
                // Si hay una agencia para precargar, la seleccionamos
                autoSelectAgencia?.let { onAgenciaSelected(it) }
            }
        }
    }

    fun onAgenciaSelected(agencia: Agencia) {
        // --- LÓGICA SIMPLIFICADA: Ya no necesita buscar la unidad de negocio ---
        _uiState.update { it.copy(agenciaSeleccionada = agencia, agenciaSearchText = agencia.nombre) }
    }

    fun onAgenciaSearchTextChanged(text: String) { _uiState.update { it.copy(agenciaSearchText = text) } }
}