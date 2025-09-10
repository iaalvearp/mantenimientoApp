// Archivo: HomeScreen.kt

package com.alpes.mantenimientoapp

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpes.mantenimientoapp.ui.theme.MantenimientoAppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel()
) {
    // KOTLIN/COMPOSE: Estos dos son los "controles remotos" para nuestro menú.
    // 'drawerState': Recuerda si el menú está abierto o cerrado.
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    // 'scope': Nos permite abrir o cerrar el menú con una animación suave.
    val scope = rememberCoroutineScope()

    val equipos by homeViewModel.uiState.collectAsStateWithLifecycle()

    // ANDROID/COMPOSE: Este es el contenedor principal que permite un menú deslizable.
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Aquí le decimos qué contenido debe mostrar el menú.
            // Llamamos a la nueva función que crearemos más abajo.
            AppDrawerContent()
        }
    ) {
        // El contenido principal de la pantalla va aquí.
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Inicio") },
                    navigationIcon = {
                        IconButton(onClick = {
                            // KOTLIN: Usamos el 'scope' para lanzar una corrutina
                            // que abre el menú con una animación.
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
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
                items(equipos) { equipo ->
                    EquipmentListItem(equipo = equipo)
                }
            }
        }
    }
}

// ANDROID/COMPOSE: Hemos creado una nueva función solo para el contenido del menú.
// Esto mantiene nuestro código limpio y ordenado.
@Composable
fun AppDrawerContent() {
    // ANDROID/COMPOSE: 'ModalDrawerSheet' es la hoja de papel sobre la que dibujamos el menú.
    ModalDrawerSheet {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Encabezado del Menú
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF57C00)) // Fondo naranja
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) // Espacio entre elementos
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // TODO: Cambia por tu logo blanco
                    contentDescription = "Logo"
                )
                // TODO: Más adelante, estos datos vendrán del usuario que hizo login.
                Text("Juan Perez", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Text("Técnico de Campo", color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }

            // 2. Opciones del Menú
            Spacer(Modifier.height(16.dp))
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                label = { Text("Inicio") },
                selected = true, // Marcamos "Inicio" como la pantalla actual
                onClick = { /* TODO: Cerrar menú */ }
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Sync, contentDescription = "Sincronizar") },
                label = { Text("Sincronizar") },
                selected = false,
                onClick = { /* TODO: Navegar a pantalla de sincronización */ }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Logout, contentDescription = "Cerrar Sesión") },
                label = { Text("Cerrar Sesión") },
                selected = false,
                onClick = { /* TODO: Lógica para cerrar sesión */ }
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MantenimientoAppTheme {
        // En la vista previa, como no podemos interactuar, no veremos el menú deslizable.
        // Pero podemos previsualizar su contenido por separado.
        AppDrawerContent()
    }
}