// Archivo: ActividadConRespuestas.kt
package com.alpes.mantenimientoapp

import androidx.room.Embedded
import androidx.room.Relation

data class ActividadConRespuestas(
    @Embedded val actividad: ActividadMantenimiento,
    @Relation(
        parentColumn = "id",
        entityColumn = "actividadId"
    )
    val posiblesRespuestas: List<PosibleRespuesta>
)