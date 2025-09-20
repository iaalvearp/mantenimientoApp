package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unidades_negocio")
data class UnidadNegocio(
    @PrimaryKey val id: Int,
    val nombre: String,
    val ciudadId: Int,
    val provinciaId: Int // <-- AÑADE ESTA LÍNEA
)