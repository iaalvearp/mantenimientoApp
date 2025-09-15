// Archivo: Tarea.kt
package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tareas")
data class Tarea(
    @PrimaryKey val id: Int,
    val clienteId: Int,
    val provinciaId: Int,
    val unidadNegocioId: Int,
    val usuarioId: Int,
    // --- CAMPOS AÑADIDOS ---
    val proyectoId: Int,
    val ciudadId: Int,
    val agenciaId: Int
)