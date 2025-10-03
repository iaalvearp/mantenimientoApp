// Archivo: MantenimientoResultado.kt (CORREGIDO)
package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mantenimiento_resultados")
data class MantenimientoResultado(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val equipoId: String,
    val actividadId: Int,
    // --- NUEVA COLUMNA ---
    // Guardará "si", "no", o null si no aplica (ej. en Diagnóstico)
    val decisionSiNo: String?,
    val respuestaValue: String?,
    val observacion: String
)