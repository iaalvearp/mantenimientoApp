// Archivo: TaskDetailViewModel.kt
package com.alpes.mantenimientoapp

import android.util.Log
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
    val unidadNegocioSeleccionada: UnidadNegocio? = null,
    val agenciaSeleccionada: Agencia? = null,
    val allClientes: List<Cliente> = emptyList(),
    val allProvincias: List<Provincia> = emptyList(),
    val proyectosFiltrados: List<Proyecto> = emptyList(),
    val ciudadesFiltradas: List<Ciudad> = emptyList(),
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
                    val unidadNegocio = dao.obtenerUnidadNegocioPorId(tarea.unidadNegocioId)
                    val agencia = dao.obtenerAgenciaPorId(tarea.agenciaId)

                    onClienteSelected(cliente, dao.obtenerProyectoPorId(tarea.proyectoId))
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

    fun onProvinciaSelected(provincia: Provincia?, autoSelectCity: Ciudad? = null, autoSelectUnidad: UnidadNegocio? = null, autoSelectAgencia: Agencia? = null) {
        _uiState.update { it.copy(
            provinciaSeleccionada = provincia, ciudadSeleccionada = null, unidadNegocioSeleccionada = null, agenciaSeleccionada = null,
            ciudadesFiltradas = emptyList(), unidadesNegocioFiltradas = emptyList(), agenciasFiltradas = emptyList(), agenciaSearchText = ""
        )}
        provincia?.let {
            viewModelScope.launch {
                val ciudades = dao.obtenerCiudadesPorProvincia(provincia.id)
                _uiState.update { it.copy(ciudadesFiltradas = ciudades) }
                autoSelectCity?.let { onCiudadSelected(it, autoSelectUnidad, autoSelectAgencia) }
            }
        }
    }

    fun onCiudadSelected(ciudad: Ciudad, autoSelectUnidad: UnidadNegocio? = null, autoSelectAgencia: Agencia? = null) {
        _uiState.update { it.copy(
            ciudadSeleccionada = ciudad, unidadNegocioSeleccionada = null, agenciaSeleccionada = null,
            unidadesNegocioFiltradas = emptyList(), agenciasFiltradas = emptyList(), agenciaSearchText = ""
        )}
        viewModelScope.launch {
            // --- INICIO DEL NUEVO BLOQUE DE DEPURACIÓN ---
            val agenciasEnLaBaseDeDatos = dao.DEBUG_getAllAgencias()
            Log.d("DEBUG_CASCADA", "INSPECCIÓN DIRECTA DE LA TABLA 'agencias':")
            agenciasEnLaBaseDeDatos.forEach { agencia ->
                Log.d("DEBUG_CASCADA", "Agencia(id=${agencia.id}, nombre='${agencia.nombre}', unidadNegocioId=${agencia.unidadNegocioId}, ciudadId=${agencia.ciudadId})")
            }
            // --- FIN DEL NUEVO BLOQUE DE DEPURACIÓN ---

            // --- SENSOR 1 ---
            Log.d("DEBUG_CASCADA", "Buscando Unidades de Negocio para ciudadId: ${ciudad.id}")
            val unidades = dao.getUnidadesNegocioByCiudad(ciudad.id)
            // --- SENSOR 2 ---
            Log.d("DEBUG_CASCADA", "Encontradas ${unidades.size} Unidades de Negocio.")

            _uiState.update { it.copy(unidadesNegocioFiltradas = unidades) }
            autoSelectUnidad?.let { onUnidadNegocioSelected(it, autoSelectAgencia) }
        }
    }

    fun onUnidadNegocioSelected(unidadNegocio: UnidadNegocio, autoSelectAgencia: Agencia? = null) {
        _uiState.update { it.copy(
            unidadNegocioSeleccionada = unidadNegocio, agenciaSeleccionada = null,
            agenciasFiltradas = emptyList(), agenciaSearchText = ""
        )}
        _uiState.value.ciudadSeleccionada?.let { ciudad ->
            viewModelScope.launch {
                // --- SENSOR 3 ---
                Log.d("DEBUG_CASCADA", "Buscando Agencias para ciudadId: ${ciudad.id} y unidadNegocioId: ${unidadNegocio.id}")
                val todasLasAgencias = dao.getAgenciasByCiudad(ciudad.id)
                val agenciasFiltradas = todasLasAgencias.filter { it.unidadNegocioId == unidadNegocio.id }
                // --- SENSOR 4 ---
                Log.d("DEBUG_CASCADA", "Encontradas ${todasLasAgencias.size} agencias en total para la ciudad. Después de filtrar, quedaron ${agenciasFiltradas.size}.")
                _uiState.update { it.copy(agenciasFiltradas = agenciasFiltradas) }
                autoSelectAgencia?.let { onAgenciaSelected(it) }
            }
        }
    }

    fun onAgenciaSelected(agencia: Agencia) {
        _uiState.update { it.copy(agenciaSeleccionada = agencia, agenciaSearchText = agencia.nombre) }
    }

    fun onAgenciaSearchTextChanged(text: String) { _uiState.update { it.copy(agenciaSearchText = text) } }
}