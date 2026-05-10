package gael.guerra.biometrics_gael.screens

import android.content.Context

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

@Composable
fun LoginScreen(innerPadding: PaddingValues, context: Context){

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authStatus by remember { mutableStateOf("") }
    var biometricAvailable by remember { mutableStateOf(true)}

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
                    authStatus = "Autenticacion exitosa. acceso concedido .|."
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

    Column() {
        Text("Iniciar sesion")
        TextField(email, onValueChange = {email=it}, label = {Text("Correo electronico")})
        TextField(password, onValueChange = {password = it}, label = {Text("Contraseña")},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
        Button(onClick = {biometricPrompt.authenticate(promptInfo)}, enabled = biometricAvailable)
        {Text("Iniciar sesion") }

        Spacer(modifier = Modifier.height(32.dp))
    }
}