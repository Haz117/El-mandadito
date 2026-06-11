package com.elmandadito.app.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.elmandadito.app.data.UserPrefsManager
import com.elmandadito.app.databinding.ActivitySplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UserPrefsManager.init(this)

        animateLogo()

        lifecycleScope.launch {
            delay(2200)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    private fun animateLogo() {
        val logo = binding.imgLogo
        val content = binding.layoutContent
        val textBrand = binding.textBrand

        logo.scaleX = 0f
        logo.scaleY = 0f
        content.alpha = 1f
        textBrand.alpha = 0f
        textBrand.translationY = 60f

        val scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0f, 1f)
        scaleX.interpolator = OvershootInterpolator(1.5f)
        scaleY.interpolator = OvershootInterpolator(1.5f)

        val logoAnim = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 600
            startDelay = 200
        }

        val slideText = ObjectAnimator.ofFloat(textBrand, "translationY", 60f, 0f).apply {
            duration = 500
            startDelay = 500
            interpolator = android.view.animation.DecelerateInterpolator(2f)
        }
        val fadeText = ObjectAnimator.ofFloat(textBrand, "alpha", 0f, 1f).apply {
            duration = 400
            startDelay = 500
        }

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
}
