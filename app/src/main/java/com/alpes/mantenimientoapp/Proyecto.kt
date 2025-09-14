package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proyectos")
data class Proyecto(
    @PrimaryKey val id: Int,
    val nombre: String
)