package com.alpes.mantenimientoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val dao: AppDao) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onUsuarioChange(usuario: String) {
        _uiState.update { it.copy(usuario = usuario, error = null) }
    }

    fun onContrasenaChange(contrasena: String) {
        _uiState.update { it.copy(contrasena = contrasena, error = null) }
    }

    fun onLoginClicked() {
        viewModelScope.launch {
            val state = uiState.value
            // Usamos la nueva función correcta del DAO
            val usuario = dao.obtenerUsuarioPorCredenciales(state.usuario, state.contrasena)

            if (usuario != null) {
                // ÉXITO: La propiedad 'id' ahora será reconocida
                _uiState.update { it.copy(loginExitoso = true, userId = usuario.id) }
            } else {
                // ERROR: Credenciales incorrectas.
                _uiState.update { it.copy(error = "Usuario o contraseña incorrectos") }
            }
        }
    }
}

data class LoginUiState(
    val usuario: String = "",
    val contrasena: String = "",
    val error: String? = null,
    val loginExitoso: Boolean = false,
    val userId: Int? = null
)