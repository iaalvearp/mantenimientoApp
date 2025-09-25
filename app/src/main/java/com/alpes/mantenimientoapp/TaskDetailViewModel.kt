package com.alpes.mantenimientoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskDetailUiState(
    val equipo: Equipo? = null,
    val tarea: Tarea? = null,
    val cliente: Cliente? = null,
    val proyecto: Proyecto? = null,
    val provincia: Provincia? = null,
    val ciudad: Ciudad? = null,
    val unidadNegocio: UnidadNegocio? = null,
    val agencia: Agencia? = null,
    val ciudadesOptions: List<Ciudad> = emptyList(),
    val unidadesNegocioOptions: List<UnidadNegocio> = emptyList(),
    val agenciasOptions: List<Agencia> = emptyList()
)

open class TaskDetailViewModel(private val dao: AppDao) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    open val uiState = _uiState.asStateFlow()

    open fun loadDataForEquipo(equipoId: String) {
        viewModelScope.launch {
            // Primero, siempre buscamos el equipo.
            val equipo = dao.obtenerEquipoPorId(equipoId)

            // Actualizamos el estado inmediatamente con la información del equipo que encontramos.
            _uiState.update { it.copy(equipo = equipo) }

            // Luego, si el equipo existe, intentamos buscar su tarea y datos relacionados.
            if (equipo != null) {
                val tarea = dao.obtenerTareaPorId(equipo.tareaId)

                // Si la tarea existe (no es un equipo local), cargamos el resto.
                if (tarea != null) {
                    val cliente = dao.obtenerClientePorId(tarea.clienteId)
                    val proyecto = dao.obtenerProyectoPorId(tarea.proyectoId)
                    val provincia = dao.obtenerProvinciaPorId(tarea.provinciaId)
                    val ciudad = dao.obtenerCiudadPorId(tarea.ciudadId)
                    val unidadNegocio = dao.obtenerUnidadNegocioPorId(tarea.unidadNegocioId)
                    val agencia = dao.obtenerAgenciaPorId(tarea.agenciaId)

                    // Obtenemos las listas de opciones para los dropdowns
                    val ciudadesOptions = provincia?.id?.let { dao.obtenerCiudadesPorProvincia(it) } ?: emptyList()
                    val unidadesNegocioOptions = provincia?.id?.let { dao.obtenerUnidadesNegocioPorProvincia(it) } ?: emptyList()
                    val agenciasOptions = unidadNegocio?.id?.let { dao.obtenerAgenciasPorUnidadNegocio(it) } ?: emptyList()

                    _uiState.update {
                        it.copy(
                            // Ya tenemos el equipo, actualizamos el resto
                            tarea = tarea,
                            cliente = cliente,
                            proyecto = proyecto,
                            provincia = provincia,
                            ciudad = ciudad,
                            unidadNegocio = unidadNegocio,
                            agencia = agencia,
                            ciudadesOptions = ciudadesOptions,
                            unidadesNegocioOptions = unidadesNegocioOptions,
                            agenciasOptions = agenciasOptions
                        )
                    }
                }
            }
        }
    }
}