package gael.guerra.biometrics_gael.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gael.guerra.biometrics_gael.datastore.DataStoreManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoggedIn: Boolean = false,
    val username: String = "",
    val biometricsActive: Boolean = false,
    val message: String = ""
)

sealed interface LoginResult {
    data object Success : LoginResult
    data object RequestBiometrics : LoginResult
    data object InvalidCredentials : LoginResult
    data object BiometricsUnavailable : LoginResult
}

class LoginViewModel(private val dataStoreManager: DataStoreManager): ViewModel() {
    private val emailCredential = "correo@mail.com"
    private val passwordCredential = "abc1234"

    val uiState: StateFlow<LoginUiState> = combine(
        dataStoreManager.isLoggedInFlow,
        dataStoreManager.usernameFlow,
        dataStoreManager.biometricsFlow
    ) { isLoggedIn, username, biometricsActive ->
        LoginUiState(
            isLoggedIn = isLoggedIn,
            username = username,
            biometricsActive = biometricsActive
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LoginUiState()
    )
    fun login(email: String, password: String, biometricsAvailable: Boolean): LoginResult {
        val cleanEmail = email.trim().lowercase()
        val canUseBiometrics = uiState.value.biometricsActive && uiState.value.username.isNotBlank()

        if (cleanEmail.isBlank() && password.isBlank() && canUseBiometrics) {
            return if (biometricsAvailable) {
                LoginResult.RequestBiometrics
            } else {
                LoginResult.BiometricsUnavailable
            }
        }

        return if (cleanEmail == emailCredential && password == passwordCredential) {
            viewModelScope.launch {
                dataStoreManager.saveSession(cleanEmail)
            }
            LoginResult.Success
        } else {
            LoginResult.InvalidCredentials
        }
    }

    fun loginWithBiometrics() {
        viewModelScope.launch {
            dataStoreManager.loginWithBiometrics()
        }
    }

    fun setBiometricsActive(active: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setBiometricsActive(active)
        }
    }

    fun logout() {
        viewModelScope.launch {
            if (uiState.value.biometricsActive) {
                dataStoreManager.logoutKeepUser()
            } else {
                dataStoreManager.logout()
            }
        }
    }
}
