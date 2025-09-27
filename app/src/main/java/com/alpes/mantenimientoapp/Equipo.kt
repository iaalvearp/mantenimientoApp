package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipos")
data class Equipo(
    @PrimaryKey val id: String,
    val nombre: String,
    val modelo: String,
    val caracteristicas: String,
    val estadoId: Int,
    val tareaId: Int,
    val syncPending: Boolean,
    val esSincronizado: Boolean = false,
    val creadoPorUsuarioId: Int? = null
)