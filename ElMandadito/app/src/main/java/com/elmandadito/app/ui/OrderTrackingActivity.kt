package com.elmandadito.app.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.elmandadito.app.R
import com.elmandadito.app.data.OrderHistoryManager
import com.elmandadito.app.databinding.ActivityOrderTrackingBinding
import com.elmandadito.app.databinding.BottomSheetRatingBinding
import com.elmandadito.app.network.repository.OrderNetworkRepository
import com.elmandadito.app.ui.profile.ReviewViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class OrderTrackingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderTrackingBinding
    private val reviewViewModel: ReviewViewModel by viewModels()
    private val handler = Handler(Looper.getMainLooper())
    private var currentStep = 0
    private var countDownTimer: CountDownTimer? = null
    private var orderNum = 1
    private var networkOrderId = 0L

    @Inject lateinit var orderRepository: OrderNetworkRepository

    private val driverNames   = listOf("Carlos M.", "Luis R.", "Miguel A.", "Jorge P.", "Andrés T.")
    private val driverRatings = listOf("4.9", "4.8", "5.0", "4.7", "4.9")

    // Polling interval for real order status (15 seconds)
    private val POLL_INTERVAL = 15_000L
    private val statusOrder = listOf("PENDING", "CONFIRMED", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderNum       = intent.getIntExtra("order_number", 1)
        networkOrderId = intent.getLongExtra("network_order_id", 0L)

        binding.textOrderNumber.text = "Pedido #${orderNum.toString().padStart(5, '0')}"

        val driverIdx = orderNum % driverNames.size
        binding.textDriverName.text    = driverNames[driverIdx]
        binding.textDriverInitial.text = driverNames[driverIdx].first().uppercase()
        binding.textDriverRating.text  = driverRatings[driverIdx]

        binding.btnBackHome.setOnClickListener { navigateHome() }
        binding.btnShareOrder.setOnClickListener { shareOrder() }

        startTracking()
        startScooterFloat()

        if (networkOrderId > 0L) scheduleStatusPoll()
    }

    // ─── Status polling ───────────────────────────────────────────────────────

    private fun scheduleStatusPoll() {
        handler.postDelayed({
            if (!isDestroyed && !isFinishing) {
                pollStatus()
                scheduleStatusPoll()
            }
        }, POLL_INTERVAL)
    }

    private fun pollStatus() {
        lifecycleScope.launch {
            orderRepository.getOrderById(networkOrderId).onSuccess { order ->
                val targetStep = when (order.status.uppercase()) {
                    "CONFIRMED"        -> 1
                    "PREPARING"        -> 2
                    "OUT_FOR_DELIVERY" -> 3
                    "DELIVERED"        -> 4
                    else               -> return@onSuccess
                }
                while (currentStep < targetStep) advanceStep()
            }
        }
    }

    // ─── UI ───────────────────────────────────────────────────────────────────

    private fun startScooterFloat() {
        ObjectAnimator.ofFloat(binding.imgScooter, "translationY", 0f, -14f, 0f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun transitionStatus(status: String, eta: String) {
        binding.textStatusMain.animate().alpha(0f).setDuration(180).withEndAction {
            binding.textStatusMain.text = status
            binding.textEta.text = eta
            binding.textStatusMain.animate().alpha(1f).setDuration(300).start()
        }.start()
    }

    private fun startTracking() {
        val delays = longArrayOf(0L, 4000L, 9000L, 15000L)
        delays.forEach { delay ->
            handler.postDelayed({ advanceStep() }, delay)
        }
    }

    private fun startCountdown(fromSeconds: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(fromSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val mins = millisUntilFinished / 1000 / 60
                val secs = (millisUntilFinished / 1000) % 60
                binding.textCountdown.text = "⏱  %02d:%02d".format(mins, secs)
            }
            override fun onFinish() { binding.textCountdown.text = "⏱  00:00" }
        }.start()
    }

    private fun advanceStep() {
        if (currentStep >= 4) return
        currentStep++
        val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        when (currentStep) {
            1 -> {
                markStepActive(binding.step1Circle, binding.step1Num, binding.step1Icon, binding.step1Time, timeNow)
                transitionStatus("Pedido confirmado", "Tiempo estimado: 35-45 min")
                animateProgress(10)
                startCountdown(40 * 60)
            }
            2 -> {
                markStepDone(binding.step1Circle, binding.step1Num, binding.step1Icon, binding.step1Time, timeNow)
                markStepActive(binding.step2Circle, binding.step2Num, binding.step2Icon, binding.step2Time, timeNow)
                transitionStatus("Preparando tu pedido", "Tiempo estimado: 25-30 min")
                animateProgress(35)
            }
            3 -> {
                markStepDone(binding.step2Circle, binding.step2Num, binding.step2Icon, binding.step2Time, timeNow)
                markStepActive(binding.step3Circle, binding.step3Num, binding.step3Icon, binding.step3Time, timeNow)
                transitionStatus("¡Tu mandadito está en camino!", "Tiempo estimado: 10-15 min")
                animateProgress(70)
                animateScooter()
                showDriverCard()
            }
            4 -> {
                markStepDone(binding.step3Circle, binding.step3Num, binding.step3Icon, binding.step3Time, timeNow)
                markStepDone(binding.step4Circle, binding.step4Num, binding.step4Icon, binding.step4Time, timeNow)
                transitionStatus("¡Pedido entregado!", "Buen provecho 🎉")
                animateProgress(100)
                countDownTimer?.cancel()
                binding.textCountdown.text = "✓  ¡Entregado!"
                binding.layoutActionButtons.visibility = View.VISIBLE
                binding.layoutActionButtons.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
                showDeliveryNotification()
                handler.postDelayed({ showRatingSheet() }, 800)
            }
        }
    }

    private fun showDriverCard() {
        binding.cardDriver.visibility = View.VISIBLE
        binding.cardDriver.alpha = 0f
        binding.cardDriver.translationY = 20f
        binding.cardDriver.animate().alpha(1f).translationY(0f).setDuration(400)
            .setInterpolator(android.view.animation.DecelerateInterpolator(2f)).start()
    }

    private fun shareOrder() {
        val text = "Pedido El Mandadito\n" +
            "Número: #${orderNum.toString().padStart(5, '0')}\n" +
            "Estado: ${binding.textStatusMain.text}\n" +
            "¡Pidiendo con El Mandadito!"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Compartir pedido"))
    }

    private fun showRatingSheet() {
        if (isFinishing || isDestroyed) return
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
                Snackbar.make(binding.root, "Por favor selecciona una calificación", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(android.graphics.Color.parseColor("#1A1A1A"))
                    .setTextColor(android.graphics.Color.WHITE)
                    .show()
                return@setOnClickListener
            }
            OrderHistoryManager.updateLatestOrderRating(selectedRating)
            // Enviar calificación a Supabase si tenemos el ID del pedido
            if (networkOrderId > 0L) {
                reviewViewModel.submitReview(networkOrderId, selectedRating, null)
            }
            dialog.dismiss()
            Snackbar.make(binding.root, "¡Gracias por tu calificación!", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(android.graphics.Color.parseColor("#1A1A1A"))
                .setTextColor(android.graphics.Color.WHITE)
                .show()
        }

        sheetBinding.btnSkipRating.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showDeliveryNotification() {
        val channelId = "order_delivered"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Pedidos", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Estado de tu pedido" }
            nm.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_scooter)
            .setContentTitle("¡Pedido entregado!")
            .setContentText("Tu mandadito ha llegado. ¡Buen provecho!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        runCatching { nm.notify(1001, notification) }
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
        countDownTimer?.cancel()
    }
}
