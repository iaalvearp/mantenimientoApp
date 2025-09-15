// Archivo: PosibleRespuesta.kt
package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posibles_respuestas")
data class PosibleRespuesta(
    @PrimaryKey val id: Int,
    val label: String, // "Muy Bien", "Bien", etc.
    val value: String  // "muy_bien", "bien", etc.
)