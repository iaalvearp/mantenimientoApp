package com.alpes.mantenimientoapp

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alpes.mantenimientoapp.ui.theme.MantenimientoAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreventiveChecklistScreen(
    viewModel: ChecklistViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checklist Preventivo") },
                navigationIcon = {
                    // IconButton(onClick = onNavigateBack) { ... }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            items(uiState.actividades) { actividad ->
                Text(text = actividad.nombre) // Mostramos el nombre de cada actividad
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreventiveChecklistScreenPreview() {
    MantenimientoAppTheme {
        // PreventiveChecklistScreen(viewModel = ..., onNavigateBack = {})
    }
}