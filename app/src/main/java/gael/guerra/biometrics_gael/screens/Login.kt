package gael.guerra.biometrics_gael.screens

import android.content.Context

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import gael.guerra.biometrics_gael.viewmodel.LoginResult
import gael.guerra.biometrics_gael.viewmodel.LoginUiState
import gael.guerra.biometrics_gael.viewmodel.LoginViewModel
import java.util.concurrent.Executor

@Composable
fun LoginScreen(
    innerPadding: PaddingValues,
    context: Context,
    viewModel: LoginViewModel,
    uiState: LoginUiState
){

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authStatus by remember { mutableStateOf("") }
    var biometricAvailable by remember { mutableStateOf(false)}

    LaunchedEffect(Unit) {
        val biometricManager: BiometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)){
            BiometricManager.BIOMETRIC_SUCCESS ->{
                authStatus = "Biometricos disponibles. presiona el boton para iniciar sesion"
                biometricAvailable = true
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->{
                authStatus = "El dispositivo no tiene sensor biometrico"
                biometricAvailable = false
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE->{
                authStatus = "sensor no encontrado"
                biometricAvailable = false
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->{
                authStatus = "Datos biometricos no registrados"
                biometricAvailable = false
            }
        }
    }

    val activity = context as FragmentActivity
    val executor: Executor = ContextCompat.getMainExecutor(context)

    val biometricPrompt: BiometricPrompt = remember {
        BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback(){
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    authStatus = "Error: ${errString}"
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    authStatus = "Autenticacion exitosa. acceso concedido."
                    viewModel.loginWithBiometrics()
                }

                override fun onAuthenticationFailed() {
                    authStatus = "Acceso denegado"
                }
            }
        )
    }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticacion biometrica")
            .setSubtitle("usa tu huella/cara para iniciar")
            .setDescription("Coluca tu dedo en el sensor o mira a la camara")
            .setNegativeButtonText("Cancelar")
            .build()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Iniciar sesión", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                when (viewModel.login(email, password, biometricAvailable)) {
                    LoginResult.Success -> authStatus = "Inicio de sesión correcto."
                    LoginResult.RequestBiometrics -> biometricPrompt.authenticate(promptInfo)
                    LoginResult.InvalidCredentials -> authStatus = "Credenciales inválidas."
                    LoginResult.BiometricsUnavailable -> authStatus = "La autenticación biométrica no está disponible."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(32.dp))
        Spacer(modifier = Modifier.height(16.dp))
        if (uiState.biometricsActive && uiState.username.isNotBlank()) {
            Text("Deja los campos vacíos y presiona Iniciar sesión para usar biometría.")
        }
        Text(authStatus)
    }
}


@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    uiState: LoginUiState,
    onBiometricsChanged: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hola ${uiState.username}",
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Autenticación biométrica")
            Switch(
                checked = uiState.biometricsActive,
                onCheckedChange = onBiometricsChanged
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}