package com.elmandadito.app.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.elmandadito.app.databinding.FragmentProfileBinding
import com.elmandadito.app.ui.auth.LoginActivity
import com.elmandadito.app.ui.detail.RestaurantDetailActivity
import com.elmandadito.app.ui.home.OrderHistoryAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActions()
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

        binding.statOrders.text = orders.size.toString()
        binding.statFavorites.text = favorites.toString()
        binding.statPoints.text = points.toString()

        val (tier, tierMin, tierMax) = OrderHistoryManager.getLoyaltyTier(points)
        val progress = if (tierMax > tierMin) ((points - tierMin) * 100 / (tierMax - tierMin)).coerceIn(0, 100) else 100
        binding.textLoyaltyTier.text = "Nivel $tier · $points pts"
        binding.progressLoyalty.progress = progress
        val nextTierPts = tierMax - points
        binding.textLoyaltyNext.text = if (points >= 600) "¡Nivel máximo!" else "Faltan $nextTierPts pts para el siguiente nivel"

        binding.layoutOrderHistorySection.visibility = if (orders.isEmpty()) View.GONE else View.VISIBLE
        binding.layoutEmptyHistory.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
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
    }

    private fun setupOrderHistory() {
        val orders = OrderHistoryManager.getOrders()
        if (orders.isNotEmpty()) {
            binding.recyclerOrderHistory.adapter = OrderHistoryAdapter(orders.take(6)) { order ->
                showOrderDetail(order)
            }
            binding.recyclerOrderHistory.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun showOrderDetail(order: OrderRecord) {
        val stars = order.ratingStars
        val ratingText = if (stars > 0)
            "${"★".repeat(stars)}${"☆".repeat(5 - stars)}  $stars/5"
        else
            "Sin calificar"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(order.restaurantName)
            .setMessage(
                "Fecha: ${order.date}\n" +
                "Platillos: ${order.itemCount} items\n" +
                "Total: $${order.total}\n" +
                "Pago: ${order.paymentMethod}\n" +
                "Calificación: $ratingText"
            )
            .setPositiveButton("Pedir de nuevo") { _, _ ->
                val restaurant = SampleData.restaurants.find { it.name == order.restaurantName }
                if (restaurant != null) {
                    val intent = Intent(requireContext(), RestaurantDetailActivity::class.java)
                    intent.putExtra("restaurant_id", restaurant.id)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                } else {
                    Snackbar.make(binding.root, "Restaurante no disponible", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cerrar", null)
            .show()
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
                .setPositiveButton("Cerrar sesión") { _, _ ->
                    UserAuthManager.logout()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    requireActivity().finish()
                }
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
