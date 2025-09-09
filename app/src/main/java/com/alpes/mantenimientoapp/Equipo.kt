package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

// ANDROID/ROOM: @Entity le dice a Room que esta clase es una tabla en la base de datos.
@Entity(tableName = "equipos")
data class Equipo(
    // ANDROID/ROOM: @PrimaryKey define que 'id' es el identificador único de cada equipo.
    @PrimaryKey val id: String,
    val nombre: String,
    val modelo: String,
    val caracteristicas: String,
    // Este campo guardará el ID del estado (1 para pendiente, 2 para en progreso, etc.)
    val estadoId: Int,
    // Este campo guardará el ID de la tarea a la que pertenece este equipo.
    val tareaId: Int
)