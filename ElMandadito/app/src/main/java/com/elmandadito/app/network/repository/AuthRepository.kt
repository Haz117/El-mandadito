package com.elmandadito.app.network.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.elmandadito.app.network.RetrofitClient
import com.elmandadito.app.network.api.AuthApi
import com.elmandadito.app.network.dto.AuthResponse
import com.elmandadito.app.network.dto.LoginRequest
import com.elmandadito.app.network.dto.RegisterRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mandadito_prefs")

class AuthRepository(private val context: Context) {

    companion object {
        private val JWT_KEY = stringPreferencesKey("jwt_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
    }

    private val tokenFlow: Flow<String?> = context.dataStore.data.map { it[JWT_KEY] }

    private val api: AuthApi by lazy {
        RetrofitClient.build { runCatching { kotlinx.coroutines.runBlocking { tokenFlow.firstOrNull() } }.getOrNull() }
            .create(AuthApi::class.java)
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> = runCatching {
        val response = api.login(LoginRequest(email, password))
        if (response.isSuccessful && response.body()?.success == true) {
            val auth = response.body()!!.data!!
            saveToken(auth.token, auth.user.id, auth.user.name)
            auth
        } else {
            throw Exception(response.body()?.message ?: "Error de login")
        }
    }

    suspend fun register(name: String, email: String, password: String, phone: String? = null): Result<AuthResponse> = runCatching {
        val response = api.register(RegisterRequest(name, email, password, phone))
        if (response.isSuccessful && response.body()?.success == true) {
            val auth = response.body()!!.data!!
            saveToken(auth.token, auth.user.id, auth.user.name)
            auth
        } else {
            throw Exception(response.body()?.message ?: "Error de registro")
        }
    }

    suspend fun getToken(): String? = tokenFlow.firstOrNull()

    suspend fun isLoggedIn(): Boolean = getToken() != null

    suspend fun logout() {
        context.dataStore.edit { it.clear() }
    }

    private suspend fun saveToken(token: String, userId: Long, name: String) {
        context.dataStore.edit { prefs ->
            prefs[JWT_KEY] = token
            prefs[USER_ID_KEY] = userId.toString()
            prefs[USER_NAME_KEY] = name
        }
    }
}
