package com.elmandadito.app.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
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
            val destination = if (UserAuthManager.isLoggedIn()) {
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
        val textBrand = binding.textBrand

        // Set initial states
        logo.scaleX = 0f
        logo.scaleY = 0f
        content.alpha = 1f         // reveal parent immediately; children animate separately
        textBrand.alpha = 0f
        textBrand.translationY = 60f

        // Logo overshoot scale-in
        val scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0f, 1f)
        scaleX.interpolator = OvershootInterpolator(1.5f)
        scaleY.interpolator = OvershootInterpolator(1.5f)

        val logoAnim = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 600
            startDelay = 200
        }

        // Brand text slides up from below + fades in
        val slideText = ObjectAnimator.ofFloat(textBrand, "translationY", 60f, 0f).apply {
            duration = 500
            startDelay = 500
            interpolator = android.view.animation.DecelerateInterpolator(2f)
        }
        val fadeText = ObjectAnimator.ofFloat(textBrand, "alpha", 0f, 1f).apply {
            duration = 400
            startDelay = 500
        }

        // Logo subtle pulse loop after it appears
        val logoPulse = ValueAnimator.ofFloat(1f, 1.05f, 1f).apply {
            duration = 1600
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            startDelay = 900
            addUpdateListener { anim ->
                val v = anim.animatedValue as Float
                logo.scaleX = v
                logo.scaleY = v
            }
        }

        AnimatorSet().apply {
            playTogether(logoAnim, slideText, fadeText)
            start()
        }
        logoPulse.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
