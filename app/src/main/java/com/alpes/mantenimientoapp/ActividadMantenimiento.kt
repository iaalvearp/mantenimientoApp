// Archivo: ActividadMantenimiento.kt (CORREGIDO)
package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

// Volvemos a una clave primaria simple, pero esta vez autogenerada
@Entity(tableName = "actividades_mantenimiento")
data class ActividadMantenimiento(
    @PrimaryKey(autoGenerate = true)
    val dbId: Int = 0, // <-- NUEVA CLAVE PRIMARIA ÚNICA

    // Mantenemos el ID del JSON como un campo normal
    val id: Int,
    val nombre: String,
    val tipo: String,
    val tipoSeleccion: String
)