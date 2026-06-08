package com.elmandadito.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import com.elmandadito.app.R
import com.elmandadito.app.data.UserAuthManager
import com.elmandadito.app.databinding.ActivityLoginBinding
import com.elmandadito.app.ui.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        com.elmandadito.app.data.UserPrefsManager.init(this)

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
    }

    private fun attemptLogin() {
        val email = binding.editEmail.text?.toString()?.trim() ?: ""
        val password = binding.editPassword.text?.toString() ?: ""

        if (email.isEmpty() || password.isEmpty()) {
            showError("Completa todos los campos")
            return
        }

        when (UserAuthManager.login(email, password)) {
            UserAuthManager.LoginResult.Success -> goToMain()
            UserAuthManager.LoginResult.NoAccount -> {
                showError("Sin cuenta registrada. Crea una primero.")
                binding.btnGoRegister.postDelayed({
                    startActivity(Intent(this, RegisterActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }, 1200)
            }
            UserAuthManager.LoginResult.WrongCredentials -> showError("Correo o contraseña incorrectos")
            UserAuthManager.LoginResult.Blocked -> showError("Cuenta bloqueada por sanciones. Contacta soporte.")
        }
    }

    private fun showError(msg: String) {
        binding.textError.text = msg
        binding.textError.visibility = View.VISIBLE
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
