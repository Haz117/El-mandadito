package com.elmandadito.app.ui.auth

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.elmandadito.app.R
import com.elmandadito.app.data.UserPrefsManager
import com.elmandadito.app.databinding.ActivityLoginBinding
import com.elmandadito.app.network.repository.AuthRepository
import com.elmandadito.app.network.repository.NotificationNetworkRepository
import com.elmandadito.app.ui.MainActivity
import com.elmandadito.app.ui.common.UiState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private var passwordVisible = false

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var notificationRepository: NotificationNetworkRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        UserPrefsManager.init(this)

        binding.editPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) { attemptLogin(); true } else false
        }

        binding.btnTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            val cursorPos = binding.editPassword.selectionEnd
            binding.editPassword.inputType = if (passwordVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.editPassword.setSelection(cursorPos.coerceAtLeast(0))
            binding.btnTogglePassword.setImageResource(
                if (passwordVisible) R.drawable.ic_eye_off else R.drawable.ic_eye
            )
        }

        binding.btnLogin.setOnClickListener { attemptLogin() }

        binding.btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.btnForgotPassword.setOnClickListener { showForgotPasswordDialog() }

        observeAuthState()
        animateEntrance()
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.btnLogin.isEnabled = false
                            binding.textError.visibility = View.GONE
                        }
                        is UiState.Success -> {
                            UserPrefsManager.setName(state.data.user.name)
                            UserPrefsManager.setEmail(state.data.user.email)
                            UserPrefsManager.setLoggedIn(true)
                            registerFcmToken()
                            goToMain()
                        }
                        is UiState.Error -> {
                            binding.btnLogin.isEnabled = true
                            showError(state.message)
                            viewModel.resetState()
                        }
                        is UiState.Idle -> binding.btnLogin.isEnabled = true
                    }
                }
            }
        }
    }

    private fun registerFcmToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            lifecycleScope.launch {
                runCatching { notificationRepository.registerToken(token) }
            }
        }
    }

    private fun showForgotPasswordDialog() {
        val input = EditText(this).apply {
            hint = "Correo electrónico"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setPadding(48, 32, 48, 32)
            setText(binding.editEmail.text)
        }
        MaterialAlertDialogBuilder(this)
            .setTitle("Recuperar contraseña")
            .setMessage("Te enviaremos un enlace para restablecer tu contraseña.")
            .setView(input)
            .setPositiveButton("Enviar") { _, _ ->
                val email = input.text.toString().trim()
                if (email.isNotEmpty()) sendRecovery(email)
                else showError("Ingresa tu correo electrónico")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun sendRecovery(email: String) {
        lifecycleScope.launch {
            authRepository.recoverPassword(email).fold(
                onSuccess = {
                    Snackbar.make(binding.root, "Correo enviado. Revisa tu bandeja de entrada.", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.parseColor("#1A1A1A"))
                        .setTextColor(Color.WHITE)
                        .show()
                },
                onFailure = { showError(it.message ?: "Error al enviar correo") }
            )
        }
    }

    private fun animateEntrance() {
        val views = listOf<View>(
            binding.layoutLogo, binding.textHeadline,
            binding.layoutEmail, binding.layoutPassword,
            binding.btnForgotPassword, binding.btnLogin
        )
        views.forEachIndexed { i, view ->
            view.alpha = 0f
            view.translationY = 42f
            view.animate()
                .alpha(1f).translationY(0f)
                .setStartDelay(100L + i * 80L)
                .setDuration(420)
                .setInterpolator(DecelerateInterpolator(2f))
                .start()
        }
    }

    private fun attemptLogin() {
        val email = binding.editEmail.text?.toString()?.trim() ?: ""
        val password = binding.editPassword.text?.toString() ?: ""

        if (email.isEmpty() || password.isEmpty()) {
            showError("Completa todos los campos")
            return
        }

        viewModel.login(email, password)
    }

    private fun showError(msg: String) {
        binding.textError.text = msg
        binding.textError.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(binding.textError, "translationX", 0f, -12f, 12f, -8f, 8f, -4f, 4f, 0f)
            .apply { duration = 450; start() }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
