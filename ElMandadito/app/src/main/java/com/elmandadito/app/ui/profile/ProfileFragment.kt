package com.elmandadito.app.ui.profile

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.elmandadito.app.R
import com.elmandadito.app.data.AddressManager
import com.elmandadito.app.data.CartRepository
import com.elmandadito.app.data.FavoritesManager
import com.elmandadito.app.data.OrderHistoryManager
import com.elmandadito.app.data.OrderRecord
import com.elmandadito.app.data.SampleData
import com.elmandadito.app.data.UserAuthManager
import com.elmandadito.app.data.UserPrefsManager
import com.elmandadito.app.databinding.BottomSheetAddressBinding
import com.elmandadito.app.databinding.BottomSheetOrderDetailBinding
import com.elmandadito.app.databinding.FragmentProfileBinding
import com.elmandadito.app.ui.auth.LoginActivity
import com.elmandadito.app.data.BusinessRepository
import com.elmandadito.app.ui.business.MyBusinessesActivity
import com.elmandadito.app.ui.detail.RestaurantDetailActivity
import com.elmandadito.app.ui.home.OrderHistoryAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private val reviewViewModel: ReviewViewModel by viewModels()

    private var lastOrderCount = -1
    private var lastFavCount = -1
    private var lastPoints = -1
    private var lastProgress = -1
    private var starsAnimated = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActions()
        observeLogout()
    }

    private fun observeLogout() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loggedOut.collect { loggedOut ->
                    if (loggedOut) {
                        UserAuthManager.logout()
                        startActivity(Intent(requireContext(), com.elmandadito.app.ui.auth.LoginActivity::class.java))
                        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        requireActivity().finish()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStats()
        setupOrderHistory()
        refreshReputation()
        binding.textProfileName.text = "¡Hola, ${UserPrefsManager.getName()}!"
        binding.textProfileEmail.text = UserPrefsManager.getEmail().ifEmpty { "Sin sesión iniciada" }
    }

    private fun refreshStats() {
        val orders = OrderHistoryManager.getOrders()
        val favorites = FavoritesManager.getFavoriteIds().size
        val points = OrderHistoryManager.getLoyaltyPoints()

        val spent = OrderHistoryManager.getTotalSpent()
        if (orders.size != lastOrderCount) { lastOrderCount = orders.size; animateStat(binding.statOrders, orders.size) }
        else binding.statOrders.text = orders.size.toString()
        if (favorites != lastFavCount) { lastFavCount = favorites; animateStat(binding.statFavorites, favorites) }
        else binding.statFavorites.text = favorites.toString()
        if (points != lastPoints) { lastPoints = points; animateStat(binding.statPoints, points) }
        else binding.statPoints.text = points.toString()
        binding.statSpent.text = "$$spent"

        val (tier, tierMin, tierMax) = OrderHistoryManager.getLoyaltyTier(points)
        val progress = if (tierMax > tierMin) ((points - tierMin) * 100 / (tierMax - tierMin)).coerceIn(0, 100) else 100
        val medal = when (tier) { "Platino" -> "💎"; "Oro" -> "🥇"; "Plata" -> "🥈"; else -> "🥉" }
        binding.textLoyaltyTier.text = "$medal Nivel $tier · $points pts"
        if (progress != lastProgress) {
            lastProgress = progress
            ObjectAnimator.ofInt(binding.progressLoyalty, "progress", 0, progress).apply {
                duration = 900; interpolator = DecelerateInterpolator(1.5f); start()
            }
        } else {
            binding.progressLoyalty.progress = progress
        }
        val nextTierPts = tierMax - points
        binding.textLoyaltyNext.text = if (points >= 600) "¡Nivel máximo!" else "Faltan $nextTierPts pts para el siguiente nivel"

        binding.layoutOrderHistorySection.visibility = if (orders.isEmpty()) View.GONE else View.VISIBLE
        binding.layoutEmptyHistory.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun animateStat(textView: android.widget.TextView, target: Int) {
        ValueAnimator.ofInt(0, target).apply {
            duration = 700
            interpolator = DecelerateInterpolator(1.5f)
            addUpdateListener { textView.text = (it.animatedValue as Int).toString() }
            start()
        }
    }

    private fun refreshReputation() {
        val stars = UserAuthManager.getStarRating()
        val sanctionCount = UserPrefsManager.getSanctionCount()
        val isBlocked = UserAuthManager.isBlocked()

        val starViews = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)
        val filled = ContextCompat.getDrawable(requireContext(), R.drawable.ic_star_filled)
        val outline = ContextCompat.getDrawable(requireContext(), R.drawable.ic_star_outline)

        starViews.forEachIndexed { index, imageView ->
            imageView.setImageDrawable(if (index < stars) filled else outline)
        }

        val (statusText, hintText, cardBg) = when {
            isBlocked || stars == 0 -> Triple(
                "Bloqueado",
                "Tu cuenta ha sido bloqueada. Contacta soporte.",
                R.drawable.bg_reputation_danger
            )
            stars <= 2 -> Triple(
                "En riesgo",
                "¡Cuidado! Otra sanción bloqueará tu cuenta.",
                R.drawable.bg_reputation_danger
            )
            stars == 3 -> Triple(
                "Precaución",
                "Has recibido sanciones. Mantén un buen comportamiento.",
                R.drawable.bg_reputation_good
            )
            else -> Triple(
                "Excelente",
                "Tu reputación es excelente. ¡Sigue así!",
                R.drawable.bg_reputation_good
            )
        }

        binding.textReputationStatus.text = statusText
        binding.textReputationHint.text = hintText
        binding.textSanctionCount.text = "$sanctionCount sanción${if (sanctionCount != 1) "es" else ""}"
        binding.layoutReputationCard.setBackgroundResource(cardBg)

        val starTint = when {
            isBlocked || stars == 0 -> "#FFCDD2"
            stars <= 2 -> "#FF8A65"
            else -> "#FFD700"
        }
        starViews.take(stars).forEach { it.setColorFilter(android.graphics.Color.parseColor(starTint)) }
        starViews.drop(stars).forEach { it.setColorFilter(android.graphics.Color.parseColor("#80FFFFFF")) }

        if (!starsAnimated) {
            starsAnimated = true
            starViews.forEachIndexed { index, imageView ->
                imageView.alpha = 0f
                imageView.scaleX = 0.4f
                imageView.scaleY = 0.4f
                imageView.animate()
                    .alpha(1f).scaleX(1f).scaleY(1f)
                    .setStartDelay(index * 70L)
                    .setDuration(280)
                    .setInterpolator(OvershootInterpolator(1.8f))
                    .start()
            }
        }
    }

    private fun setupOrderHistory() {
        val orders = OrderHistoryManager.getOrders()
        if (orders.isNotEmpty()) {
            setupHistoryFilterChips(orders)
            binding.recyclerOrderHistory.adapter = OrderHistoryAdapter(
                orders.take(6),
                onTap  = { showOrderDetail(it) },
                onRate = { showRatingDialog(it) }
            )
            binding.recyclerOrderHistory.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun showRatingDialog(order: OrderRecord) {
        if (isDetached || activity == null) return
        val d = resources.displayMetrics.density
        var selectedStars = 0

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, (16 * d).toInt(), 0, (8 * d).toInt())
        }

        val starsRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val starViews = mutableListOf<ImageView>()
        repeat(5) { i ->
            val star = ImageView(requireContext()).apply {
                setImageResource(R.drawable.ic_star_outline)
                setColorFilter(Color.parseColor("#CCCCCC"))
                layoutParams = LinearLayout.LayoutParams((44 * d).toInt(), (44 * d).toInt()).apply {
                    marginStart = (4 * d).toInt()
                    marginEnd   = (4 * d).toInt()
                }
                setOnClickListener {
                    selectedStars = i + 1
                    starViews.forEachIndexed { idx, iv ->
                        iv.setImageResource(if (idx < selectedStars) R.drawable.ic_star_filled else R.drawable.ic_star_outline)
                        iv.setColorFilter(if (idx < selectedStars) Color.parseColor("#FF9500") else Color.parseColor("#CCCCCC"))
                    }
                    starViews[i].animate().scaleX(1.3f).scaleY(1.3f).setDuration(80)
                        .withEndAction { starViews[i].animate().scaleX(1f).scaleY(1f).setDuration(120).start() }
                        .start()
                }
            }
            starViews.add(star)
            starsRow.addView(star)
        }
        container.addView(starsRow)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("¿Cómo estuvo tu pedido?")
            .setMessage(order.restaurantName)
            .setView(container)
            .setPositiveButton("Enviar calificación") { _, _ ->
                if (selectedStars > 0) {
                    OrderHistoryManager.updateLatestOrderRating(selectedStars)
                    reviewViewModel.submitReview(order.networkOrderId, selectedStars, null)
                    setupOrderHistory()
                    refreshStats()
                    Snackbar.make(binding.root, "¡Gracias por tu calificación! ${"★".repeat(selectedStars)}", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Ahora no", null)
            .show()
    }

    private fun setupHistoryFilterChips(allOrders: List<OrderRecord>) {
        val filters = listOf("Todos", "Calificados", "Sin calificar", "Este mes")
        val container = binding.layoutHistoryFilterChips
        container.removeAllViews()
        val d = resources.displayMetrics.density

        val chips = mutableListOf<android.widget.TextView>()
        filters.forEach { label ->
            val chip = android.widget.TextView(requireContext()).apply {
                text = label; textSize = 12.5f
                setPadding((14 * d).toInt(), (7 * d).toInt(), (14 * d).toInt(), (7 * d).toInt())
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = (8 * d).toInt() }
            }
            chips.add(chip)
            container.addView(chip)
        }

        fun applyFilter(filter: String) {
            chips.forEachIndexed { i, chip ->
                val sel = filters[i] == filter
                chip.background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    cornerRadius = 20f * d
                    setColor(if (sel) android.graphics.Color.parseColor("#1A1A1A") else android.graphics.Color.parseColor("#F6F6F6"))
                    setStroke((1f * d).toInt(), if (sel) android.graphics.Color.parseColor("#1A1A1A") else android.graphics.Color.parseColor("#E0E0E0"))
                }
                chip.setTextColor(if (sel) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#6B6B6B"))
            }
            val filtered = when (filter) {
                "Calificados"   -> allOrders.filter { it.ratingStars > 0 }
                "Sin calificar" -> allOrders.filter { it.ratingStars == 0 }
                "Este mes"      -> {
                    val month = java.text.SimpleDateFormat("MMM", java.util.Locale("es", "MX")).format(java.util.Date())
                    allOrders.filter { it.date.contains(month, ignoreCase = true) }
                }
                else            -> allOrders
            }
            binding.recyclerOrderHistory.adapter = OrderHistoryAdapter(filtered.take(6)) { showOrderDetail(it) }
        }

        chips.forEachIndexed { i, chip -> chip.setOnClickListener { applyFilter(filters[i]) } }
        applyFilter("Todos")
    }

    private fun showOrderDetail(order: OrderRecord) {
        if (isDetached || activity == null) return
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val b = BottomSheetOrderDetailBinding.inflate(layoutInflater)
        dialog.setContentView(b.root)

        BusinessRepository.init(requireContext())
        val allRestaurants = SampleData.restaurants + BusinessRepository.getAll().map { it.toRestaurant() }
        b.textOrderEmoji.text = allRestaurants.find { it.name == order.restaurantName }?.emoji ?: "🍽️"
        b.textOrderRestaurant.text = order.restaurantName
        b.textOrderDate.text = order.date
        b.textOrderItems.text = order.itemCount.toString()
        b.textOrderTotal.text = "$${order.total}"
        b.textOrderPaymentLabel.text = order.paymentMethod
        b.textOrderPaymentEmoji.text = when (order.paymentMethod) {
            "Tarjeta" -> "💳"; "OXXO" -> "🏪"; else -> "💵"
        }

        val starViews = listOf(b.detailStar1, b.detailStar2, b.detailStar3, b.detailStar4, b.detailStar5)
        val stars = order.ratingStars
        starViews.forEachIndexed { i, iv ->
            iv.setImageResource(if (i < stars) R.drawable.ic_star_filled else R.drawable.ic_star_outline)
            if (i < stars) iv.setColorFilter(android.graphics.Color.parseColor("#FF9500"))
            else iv.clearColorFilter()
        }
        b.textOrderRatingLabel.text = if (stars > 0) "$stars / 5 estrellas" else "Sin calificar"

        b.btnOrderAgain.setOnClickListener {
            dialog.dismiss()
            val allRestaurants = SampleData.restaurants + BusinessRepository.getAll().map { it.toRestaurant() }
            val restaurant = allRestaurants.find { it.name == order.restaurantName }
            if (restaurant != null) {
                val intent = Intent(requireContext(), RestaurantDetailActivity::class.java)
                intent.putExtra("restaurant_id", restaurant.id)
                startActivity(intent)
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } else {
                Snackbar.make(binding.root, "Restaurante no disponible", Snackbar.LENGTH_SHORT).show()
            }
        }
        b.btnCloseOrderDetail.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun setupActions() {
        binding.btnEditProfile.setOnClickListener { showEditNameDialog() }

        binding.btnOrders.setOnClickListener {
            if (OrderHistoryManager.getOrders().isNotEmpty()) {
                binding.recyclerOrderHistory.smoothScrollToPosition(0)
            } else {
                Snackbar.make(binding.root, "Aún no tienes pedidos", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.btnAddresses.setOnClickListener { showAddressSheet() }

        binding.btnRegisterBusiness.setOnClickListener {
            BusinessRepository.init(requireContext())
            startActivity(Intent(requireContext(), MyBusinessesActivity::class.java))
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.btnSupport.setOnClickListener {
            val sanctionHistory = UserPrefsManager.getSanctionHistory()
            val sanctionsText = if (sanctionHistory.isEmpty()) "Sin sanciones registradas."
            else sanctionHistory.joinToString("\n")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Soporte")
                .setMessage("¿Tienes algún problema?\n\nEscríbenos a:\nhazelalmaraz91@gmail.com\n\nHistorial de sanciones:\n$sanctionsText")
                .setPositiveButton("Aceptar", null)
                .show()
        }

        binding.btnResetData.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Restablecer datos")
                .setMessage("Se borrarán todos tus pedidos, favoritos, carrito y preferencias. ¿Continuar?")
                .setPositiveButton("Restablecer") { _, _ -> resetAllData() }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Deseas cerrar sesión?")
                .setPositiveButton("Cerrar sesión") { _, _ -> viewModel.logout() }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        setupPromoCodes()
    }

    private fun setupPromoCodes() {
        fun applyCode(code: String) {
            CartRepository.applyPromo(code)
            Snackbar.make(binding.root, "Código $code aplicado  ✓", Snackbar.LENGTH_SHORT).show()
        }
        binding.promoMandadito20.setOnClickListener { applyCode("MANDADITO20") }
        binding.promoBienvenido.setOnClickListener  { applyCode("BIENVENIDO")  }
        binding.promoPromo10.setOnClickListener     { applyCode("PROMO10")     }
    }

    private fun resetAllData() {
        CartRepository.clearCart()
        val ctx = requireContext()
        listOf("cart_prefs", "order_history_prefs", "favorites_prefs", "address_prefs", "user_prefs")
            .forEach { name -> ctx.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().apply() }
        refreshStats()
        setupOrderHistory()
        refreshReputation()
        binding.textProfileName.text = "¡Hola, ${UserPrefsManager.getName()}!"
        binding.textProfileEmail.text = ""
        Snackbar.make(binding.root, "Datos restablecidos", Snackbar.LENGTH_SHORT).show()
    }

    private fun showEditNameDialog() {
        val input = EditText(requireContext()).apply {
            setText(UserPrefsManager.getName())
            selectAll()
            hint = "Tu nombre"
            setPadding(48, 32, 48, 32)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Editar nombre")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val name = input.text.toString().trim().ifBlank { "Usuario" }
                UserPrefsManager.setName(name)
                binding.textProfileName.text = "¡Hola, $name!"
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAddressSheet() {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val sheetBinding = BottomSheetAddressBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        sheetBinding.textAddrOtherSubtitle.text = AddressManager.getCustomAddress()

        fun refreshChecks(sel: Int) {
            sheetBinding.checkAddrHome.visibility  = if (sel == 0) View.VISIBLE else View.GONE
            sheetBinding.checkAddrWork.visibility  = if (sel == 1) View.VISIBLE else View.GONE
            sheetBinding.checkAddrOther.visibility = if (sel == 2) View.VISIBLE else View.GONE
        }

        refreshChecks(AddressManager.getSelected())

        sheetBinding.addrHome.setOnClickListener  { AddressManager.setSelected(0); refreshChecks(0); dialog.dismiss() }
        sheetBinding.addrWork.setOnClickListener  { AddressManager.setSelected(1); refreshChecks(1); dialog.dismiss() }
        sheetBinding.addrOther.setOnClickListener { AddressManager.setSelected(2); refreshChecks(2); dialog.dismiss() }

        sheetBinding.btnAddAddress.setOnClickListener {
            val input = EditText(requireContext()).apply {
                setText(AddressManager.getCustomAddress())
                selectAll()
                hint = "Ej. Calle Reforma 456, Col. Juárez"
                setPadding(48, 32, 48, 32)
            }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Otra ubicación")
                .setView(input)
                .setPositiveButton("Guardar") { _, _ ->
                    AddressManager.setCustomAddress(input.text.toString())
                    sheetBinding.textAddrOtherSubtitle.text = AddressManager.getCustomAddress()
                    AddressManager.setSelected(2)
                    refreshChecks(2)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
