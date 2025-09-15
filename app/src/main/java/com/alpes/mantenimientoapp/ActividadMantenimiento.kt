// Archivo: ActividadMantenimiento.kt
package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "actividades_mantenimiento")
data class ActividadMantenimiento(
    @PrimaryKey val id: Int,
    val nombre: String
    // Podríamos añadir un campo "tipo" aquí si necesitamos diferenciar
    // entre preventivas y correctivas en la misma tabla.
)