package gael.guerra.biometrics_gael

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import gael.guerra.biometrics_gael.datastore.DataStoreManager
import gael.guerra.biometrics_gael.screens.HomeScreen
import gael.guerra.biometrics_gael.screens.LoginScreen
import gael.guerra.biometrics_gael.ui.theme.Biometrics_GaelTheme
import gael.guerra.biometrics_gael.viewmodel.LoginViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dataStoreManager = remember { DataStoreManager(applicationContext) }
            val viewModel = remember { LoginViewModel(dataStoreManager) }
            val uiState by viewModel.uiState.collectAsState()
            Biometrics_GaelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (uiState.isLoggedIn) {
                        HomeScreen(
                            innerPadding = innerPadding,
                            uiState = uiState,
                            onBiometricsChanged = viewModel::setBiometricsActive,
                            onLogout = viewModel::logout
                        )
                    } else {
                        LoginScreen(
                            innerPadding = innerPadding,
                            context = this,
                            viewModel = viewModel,
                            uiState = uiState
                        )
                    }
                }
            }
        }
    }
}

