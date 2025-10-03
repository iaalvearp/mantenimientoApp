// Archivo: HomeScreen.kt
package com.alpes.mantenimientoapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Sync
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
import androidx.room.jarjarred.org.antlr.v4.codegen.model.Sync
import com.alpes.mantenimientoapp.ui.theme.MantenimientoAppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: Int,
    homeViewModel: HomeViewModel,
    onLogout: () -> Unit,
    onEquipoClicked: (equipoId: String, numeroSerie: String) -> Unit,
    onAddEquipmentClicked: () -> Unit // <-- AÑADE ESTA LÍNEA
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
                rol = uiState.rol,
                onItemClicked = {
                    scope.launch { drawerState.close() }
                },
                onAddEquipmentClicked = onAddEquipmentClicked, // <-- CONECTA EL PARÁMETRO AQUÍ
                onLogoutClicked = onLogout
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Inicio") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, "Abrir menú")
                        }
                    }
                )
            }
        ) { innerPadding ->
            // --- INICIO DE LA ACTUALIZACIÓN ---
            LazyColumn(
                modifier = Modifier.padding(innerPadding)
            ) {
                items(uiState.equipos) { equipo ->
                    // Usamos nuestro nuevo y mejorado Composable, pasándole la acción de clic
                    EquipmentListItem(
                        equipo = equipo,
                        onClick = { onEquipoClicked(equipo.id, equipo.id) } // Pasamos el ID dos veces (como equipoId y como numeroSerie)
                    )
                }
            }
        }
    }
}

@Composable
fun AppDrawerContent(
    usuario: Usuario?,
    rol: Rol?,
    onItemClicked: () -> Unit,
    onAddEquipmentClicked: () -> Unit,
    onLogoutClicked: () -> Unit
) {
    ModalDrawerSheet(modifier = Modifier.background(Color(0xFFF57C00))) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(Color(0xFFF57C00))
                .fillMaxHeight(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logowhite),
                    contentDescription = "Logo",
                    modifier = Modifier.width(240.dp)
                )
                Text(
                    usuario?.nombre ?: "Cargando...",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    rol?.nombre ?: "Sin rol asignado",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(16.dp))

            val itemColors = NavigationDrawerItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White,
                selectedContainerColor = Color.White.copy(alpha = 0.2f),
                unselectedContainerColor = Color.Transparent
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Home, "Inicio") },
                label = { Text("Inicio") },
                selected = true,
                onClick = { onItemClicked() },
                colors = itemColors
            )
            NavigationDrawerItem(
                // --- LÍNEA CORREGIDA ---
                icon = { Icon(Icons.Filled.Sync, "Sincronizar") },
                label = { Text("Sincronizar") },
                selected = false,
                onClick = { onItemClicked() },
                colors = itemColors
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Add, "Agregar Equipo") },
                label = { Text("Agregar Equipo") },
                selected = false,
                onClick = {
                    onItemClicked()
                    onAddEquipmentClicked()
                },
                colors = itemColors
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                color = Color.White.copy(alpha = 0.5f)
            )

            NavigationDrawerItem(
                // --- LÍNEA CORREGIDA ---
                icon = { Icon(Icons.AutoMirrored.Filled.Logout, "Cerrar Sesión") },
                label = { Text("Cerrar Sesión") },
                selected = false,
                onClick = onLogoutClicked,
                colors = itemColors
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MantenimientoAppTheme {
        AppDrawerContent(
            usuario = Usuario(1, "Juan Perez", "juan.perez@example.com", "123", 1),
            rol = Rol(1, "Técnico de Campo"),
            onItemClicked = {},
            onAddEquipmentClicked = {},
            onLogoutClicked = {}
        )
    }
}