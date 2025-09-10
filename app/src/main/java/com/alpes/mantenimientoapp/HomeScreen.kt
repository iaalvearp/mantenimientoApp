// Archivo: HomeScreen.kt
package com.alpes.mantenimientoapp

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpes.mantenimientoapp.ui.theme.MantenimientoAppTheme
import android.app.Application

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    // ANDROID/COMPOSE: Obtenemos una instancia de nuestro ViewModel.
    // Compose se encargará de crearla y mantenerla viva por nosotros.
    homeViewModel: HomeViewModel = viewModel()
) {
    // ANDROID/COMPOSE: 'collectAsStateWithLifecycle' es la forma segura y recomendada
    // de "observar" un StateFlow desde la UI. Cada vez que la lista de equipos
    // en el ViewModel cambie, esta variable 'equipos' se actualizará y la UI
    // se redibujará automáticamente.
    val equipos by homeViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inicio") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Abrir menú */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Abrir menú"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) {
            // Ahora, en lugar de usar la lista de prueba, usamos la lista
            // real que viene del ViewModel.
            items(equipos) { equipo ->
                EquipmentListItem(equipo = equipo)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MantenimientoAppTheme {
        // La vista previa seguirá funcionando como antes, sin datos reales.
        HomeScreen(homeViewModel = HomeViewModel(Application()))
    }
}