package com.alpes.mantenimientoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mantenimiento_fotos")
data class MantenimientoFoto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val equipoId: String,
    val tipoFoto: String, // "preventivo" o "correctivo"
    val rutaArchivo: String, // Ruta local al archivo de imagen
    val fechaRegistro: Long = System.currentTimeMillis()
)