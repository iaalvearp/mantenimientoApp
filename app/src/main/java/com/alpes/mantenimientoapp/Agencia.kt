// Archivo: Agencia.kt (CORREGIDO)
package com.alpes.mantenimientoapp

import androidx.room.Entity

// --- CAMBIO CLAVE AQUÍ: Definimos una clave primaria compuesta ---
@Entity(tableName = "agencias", primaryKeys = ["id", "ciudadId"])
data class Agencia(
    // Ya no necesitamos @PrimaryKey aquí porque se define arriba
    val id: Int,
    val nombre: String,
    val unidadNegocioId: Int,
    val ciudadId: Int
)