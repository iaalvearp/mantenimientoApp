// Archivo: PosibleRespuesta.kt (CORREGIDO)
package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posibles_respuestas")
data class PosibleRespuesta(
    @PrimaryKey(autoGenerate = true)
    val dbId: Int = 0, // Mantenemos su propio ID único

    val id: Int,
    val label: String,
    val value: String,
    // --- CAMBIO CLAVE ---
    // Esta columna ahora se relacionará con el 'dbId' único de la Actividad
    val actividadId: Int,
    val esParaRespuestaAfirmativa: Boolean
)