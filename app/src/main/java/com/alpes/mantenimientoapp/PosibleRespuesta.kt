// Archivo: PosibleRespuesta.kt (CORREGIDO)
package com.alpes.mantenimientoapp

import androidx.room.Entity

// Mantenemos la clave primaria compuesta que ya solucionó el bug anterior
@Entity(tableName = "posibles_respuestas", primaryKeys = ["id", "actividadId"])
data class PosibleRespuesta(
    val id: Int,
    val label: String,
    val value: String,
    val actividadId: Int,
    // --- NUEVA COLUMNA ---
    // Guardará 'true' si es una respuesta para "Sí" y 'false' si es para "No"
    val esParaRespuestaAfirmativa: Boolean
)