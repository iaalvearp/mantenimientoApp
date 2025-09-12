// Archivo: HomeScreen.kt

package com.alpes.mantenimientoapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpes.mantenimientoapp.ui.theme.MantenimientoAppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: Int,
    homeViewModel: HomeViewModel = viewModel(),
    onLogout: () -> Unit // <-- CAMBIO 1: HomeScreen ahora espera recibir la función de logout.
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
                    EquipmentListItem(equipo = equipo)
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