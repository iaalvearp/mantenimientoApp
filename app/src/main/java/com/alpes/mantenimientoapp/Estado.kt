package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "estados")
data class Estado(
    @PrimaryKey val id: Int,
    val nombre: String
)