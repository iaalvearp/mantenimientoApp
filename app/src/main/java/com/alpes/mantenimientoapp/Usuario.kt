package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val id: Int,
    val nombre: String,
    val email: String,
    val password: String,
    val rolId: Int
)