package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agencias")
data class Agencia(
    @PrimaryKey val id: Int,
    val nombre: String,
    val unidadNegocioId: Int // Para saber a qué unidad de negocio pertenece
)