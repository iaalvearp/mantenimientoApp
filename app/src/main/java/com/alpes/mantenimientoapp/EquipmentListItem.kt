package com.alpes.mantenimientoapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpes.mantenimientoapp.ui.theme.MantenimientoAppTheme

@Composable
fun EquipmentListItem(equipo: Equipo, onClick: () -> Unit) {

    val (statusText, statusColor) = when (equipo.estadoId) {
        1 -> "PENDIENTE" to Color(0xFF6c757d) // Gris
        2 -> "EN PROGRESO" to Color(0xFFffc107) // Amarillo/Naranja
        3 -> "COMPLETADO" to Color(0xFF28a745) // Verde
        else -> "DESCONOCIDO" to Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            // ¡CAMBIO IMPORTANTE! Hacemos que toda la tarjeta sea clickeable.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Sin efecto visual extra al hacer clic
                onClick = onClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = equipo.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                SuggestionChip(
                    onClick = { /* No hace nada */ },
                    label = {
                        // ¡CAMBIO! Letra un poco más pequeña en el chip.
                        Text(statusText, fontSize = 11.sp)
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = statusColor,
                        labelColor = Color.White
                    ),
                    // ¡CAMBIO! Eliminamos el borde del chip.
                    border = BorderStroke(0.dp, Color.Transparent)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Modelo: ${equipo.modelo}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "No. de Serie: ${equipo.id}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Características: ${equipo.caracteristicas}",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}

// El Preview no cambia, solo se actualiza para llamar a la nueva función
@Preview(showBackground = true)
@Composable
fun EquipmentListItemPreview() {
    MantenimientoAppTheme {
        Column {
            EquipmentListItem(
                equipo = Equipo(
                    id = "21980107133GJ7000257",
                    nombre = "SWITCH CAPA 3",
                    modelo = "S5730-68C-SI-AC",
                    caracteristicas = "(48 Ethernet 10/100/1000 ports,4 10 Gig SFP+, AC 110/22V)",
                    estadoId = 2,
                    tareaId = 1,
                    syncPending = false
                ),
                onClick = {} // En el preview, el clic no hace nada
            )
            EquipmentListItem(
                equipo = Equipo(
                    id = "21980105812SJ4600371",
                    nombre = "SWITCH CAPA 2",
                    modelo = "S5720-28X-LI-AC",
                    caracteristicas = "(24 Ethernet 10/100/1000 ports, 4 10 Gig SFP+, AC 110/220V)",
                    estadoId = 3,
                    tareaId = 1,
                    syncPending = false
                ),
                onClick = {}
            )
        }
    }
}