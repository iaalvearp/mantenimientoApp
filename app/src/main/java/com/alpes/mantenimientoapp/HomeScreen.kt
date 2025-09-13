// Archivo: HomeScreen.kt

package com.alpes.mantenimientoapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alpes.mantenimientoapp.ui.theme.MantenimientoAppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: Int,
    homeViewModel: HomeViewModel,
    onLogout: () -> Unit,
    onEquipoClicked: (equipoId: String) -> Unit // Para navegar al detalle
) {
    LaunchedEffect(key1 = userId) {
        homeViewModel.loadDataForUser(userId)
    }

    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                usuario = uiState.usuario,
                onLogoutClicked = onLogout // <-- CAMBIO 2: Pasamos la función de logout al menú.
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Inicio") },
                    navigationIcon = {
                        IconButton(onClick = {
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
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(uiState.equipos) { equipo ->
                    // CORRECCIÓN: Pasamos la función onClick al item
                    EquipmentListItem(
                        equipo = equipo,
                        onClick = { onEquipoClicked(equipo.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppDrawerContent(
    usuario: Usuario?,
    onLogoutClicked: () -> Unit // <-- CAMBIO 3: El menú ahora espera recibir una función.
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ... (El encabezado del menú se mantiene igual)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF57C00))
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo"
                )
                Text(
                    usuario?.nombre ?: "Cargando...",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Técnico de Campo",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(16.dp))
            // ... (Los otros items del menú se mantienen igual)
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                label = { Text("Inicio") },
                selected = true,
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
                icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar Sesión") },
                label = { Text("Cerrar Sesión") },
                selected = false,
                onClick = onLogoutClicked // <-- CAMBIO 4: Conectamos el botón a la función recibida.
            )
        }
    }
}

// CORRECCIÓN: Este Composable necesita el parámetro onClick
@Composable
fun EquipmentListItem(equipo: Equipo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick), // Hacemos toda la tarjeta clicleable
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Router, contentDescription = "Equipo", modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(equipo.nombre, fontWeight = FontWeight.Bold)
                Text("S/N: ${equipo.id}", style = MaterialTheme.typography.bodySmall)
            }
            // Aquí podrías añadir el estado del equipo si lo tienes
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MantenimientoAppTheme {
        AppDrawerContent(
            usuario = Usuario(1, "Juan Perez", "juan.perez@example.com", "123", 1),
            onLogoutClicked = {} // <-- CAMBIO 5: En la vista previa, la función no hace nada.
        )
    }
}