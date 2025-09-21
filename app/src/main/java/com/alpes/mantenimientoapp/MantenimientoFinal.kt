package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mantenimiento_final")
data class MantenimientoFinal(
    @PrimaryKey val equipoId: String,
    val responsableCliente: String,
    val tecnicoId: Int
)