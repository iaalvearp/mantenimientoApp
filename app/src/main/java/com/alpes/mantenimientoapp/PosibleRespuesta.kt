// Archivo: PosibleRespuesta.kt
package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posibles_respuestas")
data class PosibleRespuesta(
    @PrimaryKey val id: Int,
    val label: String,
    val value: String,
    val actividadId: Int // <-- AÑADE ESTO
)