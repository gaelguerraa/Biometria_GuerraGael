package gael.guerra.biometrics_gael.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow

class DataStoreManager(private val context: Context) {

    private val Context.dataStore by preferencesDataStore("sessions_preferences")

    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USERNAME = stringPreferencesKey("username")
        val BIOMETRICS_ACTIVE = booleanPreferencesKey("biometrics")
    }

    val isLoggeedinFlow: Flow<Boolean> = context.dataStore.data.map {
        it[IS_LOGGED_IN] ?: false
    }

    val usernameFlow: Flow<String> = context.dataStore.data.map {
        it[USERNAME] ?: ""
    }

    val biometricsFlow: Flow<Boolean> = context.dataStore.data.map {
        it[BIOMETRICS_ACTIVE] ?: false
    }

    suspend fun saveSession(username: String) {
        context.dataStore.edit {
            it[IS_LOGGED_IN] = true
            it[USERNAME] = username
        }
    }

    suspend fun activeBiometrics(active: Boolean) {
        context.dataStore.edit {
            it[BIOMETRICS_ACTIVE] = active
        }
    }

    suspend fun logout() {
        context.dataStore.edit {
            it[IS_LOGGED_IN] = false
            it[USERNAME] = ""
            it[BIOMETRICS_ACTIVE] = false
        }
    }

    suspend fun logoutKeepUser() {
        context.dataStore.edit {
            it[IS_LOGGED_IN] = false
        }
    }

    suspend fun loginWithBiometrics(){
        context.dataStore.edit {
            it[IS_LOGGED_IN] = true
        }
    }

}