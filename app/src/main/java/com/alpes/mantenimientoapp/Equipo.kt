package com.alpes.mantenimientoapp
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipos")
data class Equipo(
    @PrimaryKey val id: String,
    val nombre: String,
    val modelo: String,
    val caracteristicas: String,
    var estadoId: Int,
    var tareaId: Int,
    val syncPending: Boolean = true,
    val esSincronizado: Boolean = false,
    val creadoPorUsuarioId: Int? = null
)