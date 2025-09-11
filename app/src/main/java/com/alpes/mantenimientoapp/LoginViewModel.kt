package com.alpes.mantenimientoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val dao: AppDao) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    // Este bloque se ejecuta una sola vez cuando el ViewModel es creado.
    init {
        simularObtencionDeToken()
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onContrasenaChange(contrasena: String) {
        _uiState.update { it.copy(contrasena = contrasena, error = null) }
    }

    fun onLoginClicked() {
        viewModelScope.launch {
            // 1. Mostramos que estamos cargando
            _uiState.update { it.copy(isLoading = true) }

            val state = uiState.value
            val usuario = dao.obtenerUsuarioPorCredenciales(state.email, state.contrasena)

            if (usuario != null) {
                _uiState.update { it.copy(loginExitoso = true, userId = usuario.id, isLoading = false) }
            } else {
                _uiState.update { it.copy(error = "Usuario o contraseña incorrectos", isLoading = false) }
            }
        }
    }

    private fun simularObtencionDeToken() {
        viewModelScope.launch {
            // Simulamos una espera de 2 segundos (como una llamada a una API)
            delay(1500)
            // Una vez que "obtenemos" el token, actualizamos el estado.
            _uiState.update { it.copy(isTokenReady = true) }
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val contrasena: String = "",
    val error: String? = null,
    val loginExitoso: Boolean = false,
    val userId: Int? = null,
    val isTokenReady: Boolean = false, // <-- Propiedad para el token AÑADIDA
    val isLoading: Boolean = false      // <-- Propiedad para la animación de carga AÑADIDA
)