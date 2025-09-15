// Archivo: Rol.kt
package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roles")
data class Rol(
    @PrimaryKey val id: Int,
    val nombre: String
)