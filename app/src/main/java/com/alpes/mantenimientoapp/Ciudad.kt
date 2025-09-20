package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ciudades")
data class Ciudad(
    @PrimaryKey val id: Int,
    val nombre: String,
    val provinciaId: Int // <-- AÑADE ESTA LÍNEA
)