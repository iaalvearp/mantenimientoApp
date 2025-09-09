package com.alpes.mantenimientoapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpes.mantenimientoapp.ui.theme.MantenimientoAppTheme

// ANDROID/COMPOSE: Esta es nuestra función reutilizable para dibujar una tarjeta.
// KOTLIN: Recibe como parámetro un objeto 'Equipo' para saber qué información mostrar.
@Composable
fun EquipmentListItem(equipo: Equipo) {

    // KOTLIN: 'val' declara una variable de solo lectura.
    // Aquí definimos el estado y el color basado en el 'estadoId' del equipo.
    // 'when' es como un 'switch' en otros lenguajes, es muy potente en Kotlin.
    val (statusText, statusColor) = when (equipo.estadoId) {
        1 -> "PENDIENTE" to Color(0xFF6c757d) // Gris
        2 -> "EN PROGRESO" to Color(0xFFffc107) // Amarillo/Naranja
        3 -> "COMPLETADO" to Color(0xFF28a745) // Verde
        else -> "DESCONOCIDO" to Color.Gray
    }

    // ANDROID/COMPOSE: El contenedor principal de nuestra tarjeta.
    Card(
        modifier = Modifier
            .fillMaxWidth() // Ocupa todo el ancho disponible.
            .padding(horizontal = 16.dp, vertical = 8.dp), // Espacio por fuera de la tarjeta.
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Le da una pequeña sombra.
    ) {
        // ANDROID/COMPOSE: Una columna para apilar la información verticalmente.
        Column(
            modifier = Modifier.padding(16.dp) // Espacio por dentro de la tarjeta.
        ) {
            // ANDROID/COMPOSE: Una fila para alinear el título y el estado horizontalmente.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Empuja los elementos a los extremos.
                verticalAlignment = Alignment.CenterVertically // Los centra verticalmente.
            ) {
                Text(
                    text = equipo.nombre,
                    style = MaterialTheme.typography.titleLarge, // Estilo de texto predefinido.
                    fontWeight = FontWeight.Bold
                )
                // ANDROID/COMPOSE: Un 'Chip' es ideal para mostrar estados o etiquetas.
                SuggestionChip(
                    onClick = { /* No hace nada al hacer clic por ahora */ },
                    label = { Text(statusText, fontSize = 12.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = statusColor,
                        labelColor = Color.White
                    )
                )
            }

            // ANDROID/COMPOSE: Un espacio vertical.
            Spacer(modifier = Modifier.height(16.dp))

            // Textos con la información detallada del equipo.
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
                lineHeight = 20.sp // Aumenta el espacio entre líneas si el texto es largo.
            )
        }
    }
}

// ANDROID/COMPOSE: El @Preview nos permite ver nuestro componente de forma aislada.
// Creamos un objeto 'Equipo' de mentira solo para poder visualizar el diseño.
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
                    caracteristicas = "(48 Ethernet 10/100/1000 ports,4 10 Gig SFP+, AC 110/220V)",
                    estadoId = 2, // EN PROGRESO
                    tareaId = 1
                )
            )
            EquipmentListItem(
                equipo = Equipo(
                    id = "21980105812SJ4600371",
                    nombre = "SWITCH CAPA 2",
                    modelo = "S5720-28X-LI-AC",
                    caracteristicas = "(24 Ethernet 10/100/1000 ports, 4 10 Gig SFP+, AC 110/220V)",
                    estadoId = 3, // COMPLETADO
                    tareaId = 1
                )
            )
        }
    }
}