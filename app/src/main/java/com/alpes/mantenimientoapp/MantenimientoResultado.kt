// Archivo: MantenimientoResultado.kt
package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mantenimiento_resultados")
data class MantenimientoResultado(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val equipoId: String,
    val actividadId: Int,
    val respuestaValue: String?,
    val observacion: String
)