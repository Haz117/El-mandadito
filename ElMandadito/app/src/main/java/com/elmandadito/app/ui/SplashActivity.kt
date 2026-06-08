package com.elmandadito.app.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.elmandadito.app.data.UserAuthManager
import com.elmandadito.app.data.UserPrefsManager
import com.elmandadito.app.databinding.ActivitySplashBinding
import com.elmandadito.app.ui.auth.LoginActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UserPrefsManager.init(this)

        animateLogo()

        handler.postDelayed({
            // TODO: remove DEV_MODE and restore auth check when login flow is ready
            val DEV_MODE = true
            val destination = if (DEV_MODE || UserAuthManager.isLoggedIn()) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
            startActivity(destination)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2200)
    }

    private fun animateLogo() {
        val logo = binding.imgLogo
        val content = binding.layoutContent

        logo.scaleX = 0f
        logo.scaleY = 0f

        val scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0f, 1f)
        val fadeContent = ObjectAnimator.ofFloat(content, "alpha", 0f, 1f)

        scaleX.interpolator = OvershootInterpolator(1.5f)
        scaleY.interpolator = OvershootInterpolator(1.5f)

        val logoAnim = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 600
            startDelay = 200
        }

        val contentAnim = AnimatorSet().apply {
            play(fadeContent)
            duration = 400
            startDelay = 100
        }

        AnimatorSet().apply {
            playTogether(logoAnim, contentAnim)
            start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
