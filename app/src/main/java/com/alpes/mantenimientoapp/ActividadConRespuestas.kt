// Archivo: ActividadConRespuestas.kt (CORREGIDO)
package com.alpes.mantenimientoapp

import androidx.room.Embedded
import androidx.room.Relation

data class ActividadConRespuestas(
    @Embedded val actividad: ActividadMantenimiento,
    @Relation(
        // --- CAMBIO CLAVE: La columna padre ahora es 'dbId' ---
        parentColumn = "dbId",
        entityColumn = "actividadId"
    )
    val posiblesRespuestas: List<PosibleRespuesta>
)