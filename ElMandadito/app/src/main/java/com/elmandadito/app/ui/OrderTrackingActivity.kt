package com.elmandadito.app.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.elmandadito.app.R
import com.elmandadito.app.data.OrderHistoryManager
import com.elmandadito.app.databinding.ActivityOrderTrackingBinding
import com.elmandadito.app.databinding.BottomSheetRatingBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderTrackingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderTrackingBinding
    private val handler = Handler(Looper.getMainLooper())
    private var currentStep = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val orderNum = intent.getIntExtra("order_number", 1)
        binding.textOrderNumber.text = "Pedido #${orderNum.toString().padStart(5, '0')}"

        binding.btnBackHome.setOnClickListener { navigateHome() }

        startTracking()
    }

    private fun startTracking() {
        val delays = longArrayOf(0L, 4000L, 9000L, 15000L)
        delays.forEach { delay ->
            handler.postDelayed({ advanceStep() }, delay)
        }
    }

    private fun advanceStep() {
        currentStep++
        val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        when (currentStep) {
            1 -> {
                markStepActive(binding.step1Circle, binding.step1Num, binding.step1Icon, binding.step1Time, timeNow)
                binding.textStatusMain.text = "Pedido confirmado"
                binding.textEta.text = "Tiempo estimado: 35-45 min"
                animateProgress(10)
            }
            2 -> {
                markStepDone(binding.step1Circle, binding.step1Num, binding.step1Icon, binding.step1Time, timeNow)
                markStepActive(binding.step2Circle, binding.step2Num, binding.step2Icon, binding.step2Time, timeNow)
                binding.textStatusMain.text = "Preparando tu pedido"
                binding.textEta.text = "Tiempo estimado: 25-30 min"
                animateProgress(35)
            }
            3 -> {
                markStepDone(binding.step2Circle, binding.step2Num, binding.step2Icon, binding.step2Time, timeNow)
                markStepActive(binding.step3Circle, binding.step3Num, binding.step3Icon, binding.step3Time, timeNow)
                binding.textStatusMain.text = "¡Tu mandadito está en camino!"
                binding.textEta.text = "Tiempo estimado: 10-15 min"
                animateProgress(70)
                animateScooter()
            }
            4 -> {
                markStepDone(binding.step3Circle, binding.step3Num, binding.step3Icon, binding.step3Time, timeNow)
                markStepDone(binding.step4Circle, binding.step4Num, binding.step4Icon, binding.step4Time, timeNow)
                binding.textStatusMain.text = "¡Pedido entregado!"
                binding.textEta.text = "Buen provecho"
                animateProgress(100)
                binding.btnBackHome.visibility = View.VISIBLE
                binding.btnBackHome.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
                handler.postDelayed({ showRatingSheet() }, 800)
            }
        }
    }

    private fun showRatingSheet() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val sheetBinding = BottomSheetRatingBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)
        dialog.setCancelable(false)

        val stars = listOf(sheetBinding.star1, sheetBinding.star2,
            sheetBinding.star3, sheetBinding.star4, sheetBinding.star5)

        var selectedRating = 0
        val ratingLabels = listOf("", "Muy malo", "Malo", "Regular", "Bueno", "Excelente")

        fun updateStars(rating: Int) {
            selectedRating = rating
            stars.forEachIndexed { index, star ->
                val filled = index < rating
                star.setImageResource(if (filled) R.drawable.ic_star_filled else R.drawable.ic_star_outline)
                if (filled) star.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up))
            }
            sheetBinding.textRatingLabel.text = if (rating > 0) ratingLabels[rating] else "Toca para calificar"
        }

        stars.forEachIndexed { index, star ->
            star.setOnClickListener { updateStars(index + 1) }
        }

        sheetBinding.btnSubmitRating.setOnClickListener {
            if (selectedRating == 0) {
                Toast.makeText(this, "Por favor selecciona una calificación", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            OrderHistoryManager.updateLatestOrderRating(selectedRating)
            dialog.dismiss()
            Toast.makeText(this, "¡Gracias por tu calificación!", Toast.LENGTH_SHORT).show()
        }

        sheetBinding.btnSkipRating.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun navigateHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun markStepActive(
        circle: View, num: android.widget.TextView,
        icon: ImageView, time: android.widget.TextView, timeStr: String
    ) {
        circle.setBackgroundResource(R.drawable.bg_step_active)
        num.setTextColor(getColor(android.R.color.white))
        time.text = "Ahora · $timeStr"
        ObjectAnimator.ofFloat(circle, "scaleX", 1f, 1.15f, 1f).apply {
            duration = 600; repeatCount = 2; interpolator = OvershootInterpolator(); start()
        }
        ObjectAnimator.ofFloat(circle, "scaleY", 1f, 1.15f, 1f).apply {
            duration = 600; repeatCount = 2; interpolator = OvershootInterpolator(); start()
        }
    }

    private fun markStepDone(
        circle: View, num: android.widget.TextView,
        icon: ImageView, time: android.widget.TextView, timeStr: String
    ) {
        circle.setBackgroundResource(R.drawable.bg_step_done)
        num.visibility = View.GONE
        icon.visibility = View.VISIBLE
        time.text = "Completado · $timeStr"
    }

    private fun animateProgress(target: Int) {
        ObjectAnimator.ofInt(binding.progressDelivery, "progress", binding.progressDelivery.progress, target)
            .apply { duration = 800; start() }
    }

    private fun animateScooter() {
        ObjectAnimator.ofFloat(binding.imgScooter, "translationX", -20f, 20f, -20f, 20f, 0f)
            .apply { duration = 800; start() }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
