// Archivo: EquipoSimple.kt
package com.alpes.mantenimientoapp

// Esta clase no es una tabla, solo sirve para recibir resultados de consultas parciales.
data class EquipoSimple(
    val nombre: String,
    val modelo: String,
    val caracteristicas: String
)