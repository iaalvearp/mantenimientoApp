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
    val tarea: Tarea? = null,
    val cliente: Cliente? = null,
    val proyecto: Proyecto? = null,
    val provincia: Provincia? = null
)

open class TaskDetailViewModel(private val dao: AppDao) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    open val uiState = _uiState.asStateFlow()

    open fun loadDataForEquipo(equipoId: String) {
        viewModelScope.launch {
            val equipo = dao.obtenerEquipoPorId(equipoId)
            if (equipo != null) {
                val tarea = dao.obtenerTareaPorId(equipo.tareaId)

                val cliente = tarea?.clienteId?.let { dao.obtenerClientePorId(it) }
                val proyecto = tarea?.proyectoId?.let { dao.obtenerProyectoPorId(it) }
                val provincia = tarea?.provinciaId?.let { dao.obtenerProvinciaPorId(it) }

                _uiState.update {
                    it.copy(
                        equipo = equipo,
                        tarea = tarea,
                        cliente = cliente,
                        proyecto = proyecto,
                        provincia = provincia
                    )
                }
            }
        }
    }
}