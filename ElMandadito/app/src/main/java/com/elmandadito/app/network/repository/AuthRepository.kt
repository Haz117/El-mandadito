package com.elmandadito.app.network.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.elmandadito.app.network.RetrofitClient
import com.elmandadito.app.network.api.AuthApi
import com.elmandadito.app.network.api.SupabaseLoginRequest
import com.elmandadito.app.network.api.SupabaseSignUpRequest
import com.elmandadito.app.network.dto.AuthResponse
import com.elmandadito.app.network.dto.AuthUserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.net.SocketTimeoutException

class AuthRepository(private val context: Context) {

    companion object {
        private val JWT_KEY     = stringPreferencesKey("jwt_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    private val tokenFlow: Flow<String?> = context.dataStore.data.map { it[JWT_KEY] }

    private val api: AuthApi by lazy {
        RetrofitClient.buildAuth().create(AuthApi::class.java)
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> = safe {
        val response = api.login(request = SupabaseLoginRequest(email, password))
        if (response.isSuccessful && response.body() != null) {
            val sb = response.body()!!
            saveSession(sb.accessToken, sb.user.id, sb.user.userMetadata["name"] as? String ?: "", email)
            AuthResponse(
                token = sb.accessToken,
                user  = AuthUserDto(
                    id              = sb.user.id.hashCode().toLong(),
                    name            = sb.user.userMetadata["name"] as? String ?: "",
                    email           = sb.user.email,
                    role            = "USER",
                    profileImageUrl = null,
                    mandapoints     = 0
                )
            )
        } else {
            throw Exception("Email o contraseña incorrectos")
        }
    }

    suspend fun register(name: String, email: String, password: String, phone: String? = null): Result<AuthResponse> = safe {
        val meta = buildMap<String, String> {
            put("name", name)
            if (phone != null) put("phone", phone)
        }
        val response = api.register(SupabaseSignUpRequest(email, password, meta))
        if (response.isSuccessful && response.body() != null) {
            val sb = response.body()!!
            saveSession(sb.accessToken, sb.user.id, name, email)
            AuthResponse(
                token = sb.accessToken,
                user  = AuthUserDto(
                    id              = sb.user.id.hashCode().toLong(),
                    name            = name,
                    email           = sb.user.email,
                    role            = "USER",
                    profileImageUrl = null,
                    mandapoints     = 0
                )
            )
        } else {
            throw Exception("Error al crear la cuenta. Verifica que el email no esté registrado.")
        }
    }

    suspend fun getToken(): String?  = tokenFlow.firstOrNull()
    suspend fun getUserId(): String? = context.dataStore.data.map { it[USER_ID_KEY] }.firstOrNull()
    suspend fun isLoggedIn(): Boolean = getToken() != null

    suspend fun logout() {
        val token = getToken()
        if (token != null) runCatching { api.logout("Bearer $token") }
        context.dataStore.edit { it.clear() }
    }

    private suspend fun saveSession(token: String, userId: String, name: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[JWT_KEY]       = token
            prefs[USER_ID_KEY]   = userId
            prefs[USER_NAME_KEY] = name
            prefs[USER_EMAIL_KEY] = email
        }
    }

    private suspend fun <T> safe(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: SocketTimeoutException) {
        Result.failure(Exception("El servidor tardó demasiado en responder"))
    } catch (e: IOException) {
        Result.failure(Exception("Sin conexión a internet"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
