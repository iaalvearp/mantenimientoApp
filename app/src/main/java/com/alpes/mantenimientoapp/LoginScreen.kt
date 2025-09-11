// Archivo: LoginScreen.kt

package com.alpes.mantenimientoapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect


@Composable
fun LoginScreen(
    onLoginSuccess: (Int) -> Unit,
    loginViewModel: LoginViewModel // <-- Quitamos el "= viewModel()"
) {
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    // Este bloque se ejecutará cada vez que uiState.loginExitoso cambie a true
    LaunchedEffect(uiState.loginExitoso) {
        if (uiState.loginExitoso && uiState.userId != null) {
            onLoginSuccess(uiState.userId!!)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo de la empresa",
                    modifier = Modifier.height(80.dp).width(240.dp) // O .size(120.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color(0xFFF57C00))
                Spacer(modifier = Modifier.height(32.dp))

                Text(text = "INICIAR SESIÓN", fontSize = 20.sp, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = uiState.usuario, // Leemos el valor del ViewModel
                    onValueChange = { loginViewModel.onUsuarioChange(it) }, // Notificamos al ViewModel
                    label = { Text("Usuario") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.contrasena, // Leemos el valor del ViewModel
                    onValueChange = { loginViewModel.onContrasenaChange(it) }, // Notificamos al ViewModel
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = uiState.error != null
                )

                // 1. "Tomamos la foto" del estado del error.
                val errorActual = uiState.error

                // 2. Comprobamos la foto.
                if (errorActual != null) {
                    Text(
                        text = errorActual, // 3. Usamos la foto, que es 100% segura.
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { loginViewModel.onLoginClicked() },) {
                    Text(
                        text = "Recuperar contraseña",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color(0xFFF57C00)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { loginViewModel.onLoginClicked() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                ) {
                    Text(text = "Ingresar", fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }
}
