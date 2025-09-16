// Archivo: ActividadMantenimiento.kt
package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "actividades_mantenimiento")
data class ActividadMantenimiento(
    @PrimaryKey val id: Int,
    val nombre: String,
    val tipo: String // "preventivo" o "correctivo"
)