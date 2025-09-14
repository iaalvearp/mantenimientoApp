package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey val id: Int,
    val nombre: String,
    val nombreCompleto: String
)

