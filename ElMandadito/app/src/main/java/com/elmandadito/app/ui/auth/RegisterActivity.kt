package com.elmandadito.app.ui.auth

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import com.elmandadito.app.R
import com.elmandadito.app.data.UserAuthManager
import com.elmandadito.app.databinding.ActivityRegisterBinding
import com.elmandadito.app.ui.MainActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var passwordVisible = false
    private var confirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        com.elmandadito.app.data.UserPrefsManager.init(this)

        binding.editConfirmPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) { attemptRegister(); true } else false
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

        binding.btnToggleConfirmPassword.setOnClickListener {
            confirmPasswordVisible = !confirmPasswordVisible
            val cursorPos = binding.editConfirmPassword.selectionEnd
            binding.editConfirmPassword.inputType = if (confirmPasswordVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.editConfirmPassword.setSelection(cursorPos.coerceAtLeast(0))
            binding.btnToggleConfirmPassword.setImageResource(
                if (confirmPasswordVisible) R.drawable.ic_eye_off else R.drawable.ic_eye
            )
        }

        binding.btnRegister.setOnClickListener { attemptRegister() }

        binding.btnGoLogin.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        animateEntrance()
    }

    private fun animateEntrance() {
        val views = listOf<View>(
            binding.layoutLogo, binding.textHeadline,
            binding.layoutName, binding.layoutEmail,
            binding.layoutCurp, binding.layoutPassword,
            binding.layoutConfirmPassword, binding.btnRegister
        )
        views.forEachIndexed { i, view ->
            view.alpha = 0f
            view.translationY = 42f
            view.animate()
                .alpha(1f).translationY(0f)
                .setStartDelay(80L + i * 60L)
                .setDuration(400)
                .setInterpolator(DecelerateInterpolator(2f))
                .start()
        }
    }

    private fun attemptRegister() {
        val name = binding.editName.text?.toString()?.trim() ?: ""
        val email = binding.editEmail.text?.toString()?.trim() ?: ""
        val curp = binding.editCurp.text?.toString()?.trim() ?: ""
        val password = binding.editPassword.text?.toString() ?: ""
        val confirm = binding.editConfirmPassword.text?.toString() ?: ""

        if (name.isEmpty() || email.isEmpty() || curp.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("Completa todos los campos")
            return
        }
        if (password != confirm) {
            showError("Las contraseñas no coinciden")
            return
        }

        when (val result = UserAuthManager.register(name, email, password, curp)) {
            UserAuthManager.RegisterResult.Success -> goToMain()
            is UserAuthManager.RegisterResult.Error -> showError(result.message)
        }
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
