// Archivo: FinalizacionScreen.kt (REEMPLAZAR COMPLETO)
package com.alpes.mantenimientoapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility // <-- AÑADIR IMPORTACIÓN
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.alpes.mantenimientoapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalizacionScreen(
    equipoId: String,
    numeroSerie: String,
    viewModel: FinalizacionViewModel,
    onNavigateBackToHome: (Int) -> Unit,
    onBackClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var currentPhotoType by remember { mutableStateOf("") }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Launchers (sin cambios)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempPhotoUri?.let { viewModel.addPhotoUri(it, currentPhotoType) }
            }
        }
    )
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            uris.forEach { uri ->
                viewModel.addPhotoUri(uri, currentPhotoType)
            }
        }
    )
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val uri = createTempUri(context)
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            }
        }
    )

    // LaunchedEffects (sin cambios)
    LaunchedEffect(key1 = equipoId) { viewModel.loadData(equipoId) }
    LaunchedEffect(key1 = uiState.finalizacionExitosa) {
        if (uiState.finalizacionExitosa) { onNavigateBackToHome(uiState.userId) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finalización de Mantenimiento") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Info (sin cambios)
            InfoRow(label = "Técnico Responsable:", value = uiState.tecnico?.nombre ?: "Cargando...")
            InfoRow(label = "Cliente:", value = uiState.cliente?.nombreCompleto ?: "Cargando...")
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Delegado:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                OutlinedTextField(
                    value = uiState.responsableCliente,
                    onValueChange = { viewModel.onResponsableChanged(it) },
                    label = { Text("Nombre del Responsable del Cliente") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            // --- REQ #9: SECCIÓN DE FOTOS PREVENTIVO CONDICIONAL ---
            AnimatedVisibility(visible = uiState.isPreventiveCompleted) {
                PhotoAttachmentSection(
                    label = "Fotos Preventivo",
                    photoUris = uiState.fotosPreventivas,
                    onAddFromCamera = {
                        currentPhotoType = "preventivo"
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                                val uri = createTempUri(context)
                                tempPhotoUri = uri
                                cameraLauncher.launch(uri)
                            }
                            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onAddFromGallery = {
                        currentPhotoType = "preventivo"
                        galleryLauncher.launch("image/*")
                    },
                    // --- REQ #10: Pasamos el estado de validación ---
                    isAddEnabled = uiState.isMaxPhotoRequirementMet
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- REQ #9: SECCIÓN DE FOTOS CORRECTIVO CONDICIONAL ---
            AnimatedVisibility(visible = uiState.isCorrectiveCompleted) {
                PhotoAttachmentSection(
                    label = "Fotos Correctivo",
                    photoUris = uiState.fotosCorrectivas,
                    onAddFromCamera = {
                        currentPhotoType = "correctivo"
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                                val uri = createTempUri(context)
                                tempPhotoUri = uri
                                cameraLauncher.launch(uri)
                            }
                            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onAddFromGallery = {
                        currentPhotoType = "correctivo"
                        galleryLauncher.launch("image/*")
                    },
                    // --- REQ #10: Pasamos el estado de validación ---
                    isAddEnabled = uiState.isMaxPhotoRequirementMet
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.weight(1f)) // Empuja el botón hacia abajo

            // --- REQ #11: Lógica de 'enabled' actualizada ---
            val isButtonEnabled = uiState.responsableCliente.isNotBlank() && uiState.isMinPhotoRequirementMet
            Button(
                onClick = { viewModel.saveAndFinalize(equipoId, numeroSerie) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = isButtonEnabled, // <-- Conectado al estado
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isButtonEnabled) Color(0xFFF57C00) else MaterialTheme.colorScheme.surfaceVariant
                )
            ) { Text("FINALIZAR") }
        }

        // --- Diálogo de validación para Mín/Máx de fotos ---
        if (uiState.validationError != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissValidationError() },
                title = { Text("Error de Validación") },
                text = { Text(uiState.validationError!!) },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissValidationError() }) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}

// --- COMPOSABLE PhotoAttachmentSection MODIFICADO ---
@Composable
private fun PhotoAttachmentSection(
    label: String,
    photoUris: List<Uri>,
    onAddFromCamera: () -> Unit,
    onAddFromGallery: () -> Unit,
    isAddEnabled: Boolean // <-- REQ #10: Nuevo parámetro
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label (${photoUris.size})", // Mostramos el conteo
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (photoUris.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                photoUris.forEach { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Foto adjunta",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAddFromCamera,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33A8FF)),
                enabled = isAddEnabled // <-- REQ #10: Desactiva si llega a 6
            ) {
                Icon(Icons.Filled.AddAPhoto, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cámara")
            }
            Button(
                onClick = onAddFromGallery,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33A8FF)),
                enabled = isAddEnabled // <-- REQ #10: Desactiva si llega a 6
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.gallery),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Galería")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// (createTempUri e InfoRow se mantienen igual)
private fun createTempUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val file = File(context.cacheDir, "JPEG_${timeStamp}_.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
    }
}