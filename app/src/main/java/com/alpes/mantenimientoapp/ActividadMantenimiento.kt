// Archivo: ActividadMantenimiento.kt (CORREGIDO)
package com.alpes.mantenimientoapp

import androidx.room.Entity

// --- CAMBIO CLAVE: Usamos una clave primaria compuesta ---
@Entity(tableName = "actividades_mantenimiento", primaryKeys = ["id", "tipo"])
data class ActividadMantenimiento(
    val id: Int,
    val nombre: String,
    val tipo: String,
    val tipoSeleccion: String
)