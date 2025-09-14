// Archivo: TaskDetailScreen.kt
package com.alpes.mantenimientoapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alpes.mantenimientoapp.ui.theme.MantenimientoAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    equipoId: String,
    viewModel: TaskDetailViewModel,
    onNavigateBack: () -> Unit,
    onNextClicked: () -> Unit
) {
    LaunchedEffect(key1 = equipoId) {
        viewModel.loadDataForEquipo(equipoId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val equipo = uiState.equipo
    val cliente = uiState.cliente
    val proyecto = uiState.proyecto
    val provincia = uiState.provincia

    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF33A8FF)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = { Text("Ficha de Mantenimiento") },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 8.dp, vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("INFORMACIÓN GENERAL", style = MaterialTheme.typography.titleMedium)
                        DropdownField(label = "Cliente", options = listOf(cliente?.nombreCompleto ?: "Cargando..."), isTall = true)
                        DropdownField(label = "Nombre del proyecto", options = listOf(proyecto?.nombre ?: "Cargando..."), isTall = true)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) { DropdownField(label = "Provincia", options = listOf(provincia?.nombre ?: "Cargando...")) }
                            Box(modifier = Modifier.weight(1f)) { DropdownField(label = "Ciudad", options = listOf("GUAYAQUIL")) }
                        }
                        DropdownField(label = "Unidad de negocio", options = listOf("UNIDAD XYZ"))
                        DateField(selectedDateMillis = selectedDateMillis, onClick = { showDatePicker = true })
                        DropdownField(label = "Agencia, oficina, subestación", options = listOf("SUBESTACIÓN MANTA 1..."), isTall = true)

                        Text("INFORMACIÓN DE EQUIPO", style = MaterialTheme.typography.titleMedium)
                        DropdownField(label = "Tipo de equipo", options = listOf(equipo?.nombre ?: "Cargando..."))
                        DropdownField(label = "Número de serie", options = listOf(equipo?.id ?: "Cargando..."))
                        DropdownField(label = "Modelo del equipo", options = listOf(equipo?.modelo ?: "Cargando..."))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNextClicked,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                    ) {
                        Text("SIGUIENTE")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}


// --- FUNCIONES AUXILIARES ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    options: List<String>,
    initialValue: String = options.firstOrNull() ?: "",
    isTall: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(initialValue) }
    val heightModifier = if (isTall) Modifier.heightIn(min = 80.dp) else Modifier

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .then(heightModifier),
            readOnly = true,
            value = selectedOptionText,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(unfocusedContainerColor = Color(0xFFF0F0F0)),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        selectedOptionText = selectionOption
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DateField(selectedDateMillis: Long, onClick: () -> Unit) {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateString = formatter.format(Date(selectedDateMillis))

    OutlinedTextField(
        value = dateString,
        onValueChange = {},
        label = { Text("Fecha") },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha", Modifier.clickable(onClick = onClick))
        }
    )
}

// --- VISTA PREVIA Y CLASES FALSAS (MOCKS) ---

private class FakeAppDao : AppDao {
    override suspend fun insertarUsuario(usuario: Usuario) {}
    override suspend fun insertarTarea(tarea: Tarea) {}
    override suspend fun insertarEquipo(equipo: Equipo) {}
    override suspend fun insertarEstado(estado: Estado) {}
    override suspend fun insertarCliente(cliente: Cliente) {}
    override suspend fun insertarProyecto(proyecto: Proyecto) {}
    override suspend fun insertarProvincia(provincia: Provincia) {}
    override suspend fun obtenerEquiposPorTarea(idDeLaTarea: Int): List<Equipo> = emptyList()
    override suspend fun obtenerTareasPorUsuario(idDelUsuario: Int): List<Tarea> = emptyList()
    override suspend fun obtenerUsuarioPorId(userId: Int): Usuario? = null
    override suspend fun obtenerUsuarioPorCredenciales(email: String, password: String): Usuario? = null
    override suspend fun obtenerEquipoPorId(equipoId: String): Equipo? = null
    override suspend fun obtenerTareaPorId(tareaId: Int): Tarea? = null
    override suspend fun obtenerClientePorId(clienteId: Int): Cliente? = null
    override suspend fun obtenerProyectoPorId(proyectoId: Int): Proyecto? = null
    override suspend fun obtenerProvinciaPorId(provinciaId: Int): Provincia? = null
}

private class PreviewTaskDetailViewModel(initialState: TaskDetailUiState) : TaskDetailViewModel(dao = FakeAppDao()) {
    private val _uiState = MutableStateFlow(initialState)
    override val uiState = _uiState.asStateFlow()
    override fun loadDataForEquipo(equipoId: String) {}
}

@Preview(showBackground = true)
@Composable
fun TaskDetailScreenPreview() {
    val previewState = TaskDetailUiState(
        equipo = Equipo("PREVIEW-ID-123", "ROUTER DE PRUEBA", "MODELO-XYZ", "", 1, 1),
        cliente = Cliente(1, "CNEL", "CORPORACIÓN NACIONAL DE ELECTRICIDAD"),
        proyecto = Proyecto(1, "SOPORTE Y MANTENIMIENTO"),
        provincia = Provincia(1, "GUAYAS")
    )

    val previewViewModel = remember { PreviewTaskDetailViewModel(previewState) }

    MantenimientoAppTheme {
        TaskDetailScreen(
            equipoId = "PREVIEW-ID-123",
            viewModel = previewViewModel,
            onNavigateBack = {},
            onNextClicked = {}
        )
    }
}