package com.elmandadito.app.data

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

object UserAuthManager {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private val curpRegex = Regex("^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z0-9]{2}$")

    fun validateCurp(curp: String): Boolean = curpRegex.matches(curp.uppercase().trim())

    fun register(name: String, email: String, password: String, curp: String): RegisterResult {
        if (name.isBlank()) return RegisterResult.Error("El nombre es obligatorio")
        if (!email.contains("@") || !email.contains(".")) return RegisterResult.Error("Correo inválido")
        if (password.length < 6) return RegisterResult.Error("La contraseña debe tener al menos 6 caracteres")
        if (curp.isBlank()) return RegisterResult.Error("El CURP es obligatorio")
        if (!validateCurp(curp)) return RegisterResult.Error("CURP inválido. Formato: AAAA######HAAAAAA##")
        if (UserPrefsManager.hasAccount() &&
            email.trim().equals(UserPrefsManager.getEmail(), ignoreCase = true)) {
            return RegisterResult.Error("Ya existe una cuenta con este correo")
        }
        if (UserPrefsManager.hasAccount() &&
            curp.uppercase().trim() == UserPrefsManager.getCurp() &&
            UserPrefsManager.getCurp().isNotBlank()) {
            return RegisterResult.Error("Ya existe una cuenta con este CURP")
        }
        UserPrefsManager.setName(name.trim())
        UserPrefsManager.setEmail(email.trim().lowercase())
        UserPrefsManager.setPasswordHash(hashPassword(password))
        UserPrefsManager.setCurp(curp.uppercase().trim())
        UserPrefsManager.setLoggedIn(true)
        UserPrefsManager.setStarRating(5)
        UserPrefsManager.setBlocked(false)
        return RegisterResult.Success
    }

    fun login(email: String, password: String): LoginResult {
        if (!UserPrefsManager.hasAccount()) return LoginResult.NoAccount
        val storedEmail = UserPrefsManager.getEmail()
        val storedHash = UserPrefsManager.getPasswordHash()
        if (!email.trim().equals(storedEmail, ignoreCase = true)) return LoginResult.WrongCredentials
        if (hashPassword(password) != storedHash) return LoginResult.WrongCredentials
        if (UserPrefsManager.isBlocked()) return LoginResult.Blocked
        UserPrefsManager.setLoggedIn(true)
        return LoginResult.Success
    }

    fun logout() = UserPrefsManager.setLoggedIn(false)

    fun applyUserSanction(reason: String) {
        val currentStars = UserPrefsManager.getStarRating()
        val newStars = (currentStars - 1).coerceAtLeast(0)
        UserPrefsManager.setStarRating(newStars)
        UserPrefsManager.incrementSanctions()
        val entry = "${dateFormat.format(Date())} — $reason"
        UserPrefsManager.addSanction(entry)
        if (newStars == 0) UserPrefsManager.setBlocked(true)
    }

    fun isLoggedIn(): Boolean = UserPrefsManager.isLoggedIn()
    fun isBlocked(): Boolean = UserPrefsManager.isBlocked()
    fun getStarRating(): Int = UserPrefsManager.getStarRating()

    sealed class LoginResult {
        object Success : LoginResult()
        object NoAccount : LoginResult()
        object WrongCredentials : LoginResult()
        object Blocked : LoginResult()
    }

    sealed class RegisterResult {
        object Success : RegisterResult()
        data class Error(val message: String) : RegisterResult()
    }
}
