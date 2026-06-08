package com.elmandadito.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import com.elmandadito.app.data.UserAuthManager
import com.elmandadito.app.databinding.ActivityRegisterBinding
import com.elmandadito.app.ui.MainActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        com.elmandadito.app.data.UserPrefsManager.init(this)

        binding.editConfirmPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) { attemptRegister(); true } else false
        }

        binding.btnRegister.setOnClickListener { attemptRegister() }

        binding.btnGoLogin.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun attemptRegister() {
        val name = binding.editName.text?.toString()?.trim() ?: ""
        val email = binding.editEmail.text?.toString()?.trim() ?: ""
        val password = binding.editPassword.text?.toString() ?: ""
        val confirm = binding.editConfirmPassword.text?.toString() ?: ""

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("Completa todos los campos")
            return
        }
        if (password != confirm) {
            showError("Las contraseñas no coinciden")
            return
        }

        when (val result = UserAuthManager.register(name, email, password)) {
            UserAuthManager.RegisterResult.Success -> goToMain()
            is UserAuthManager.RegisterResult.Error -> showError(result.message)
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
