package com.elmandadito.app.ui.cart

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.elmandadito.app.R
import com.elmandadito.app.data.AddressManager
import com.elmandadito.app.data.CartItem
import com.elmandadito.app.data.CartRepository
import com.elmandadito.app.data.OrderHistoryManager
import com.elmandadito.app.data.UserAuthManager
import com.elmandadito.app.databinding.BottomSheetPaymentBinding
import com.elmandadito.app.databinding.FragmentCartBinding
import com.elmandadito.app.ui.OrderTrackingActivity
import com.elmandadito.app.ui.auth.LoginActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlin.random.Random

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CartAdapter

    private var selectedPayment = "cash"
    private var contentShown = false
    private var scooterAnimator: ObjectAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CartAdapter(
            onIncrease = { CartRepository.increaseQty(it) },
            onDecrease = { CartRepository.decreaseQty(it) }
        )
        binding.recyclerCart.adapter = adapter
        setupSwipeToDelete()
        startScooterFloat()

        CartRepository.items.observe(viewLifecycleOwner) { items ->
            if (items.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.layoutContent.visibility = View.GONE
                binding.btnClearCart.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.btnClearCart.visibility = View.VISIBLE
                if (!contentShown) {
                    contentShown = true
                    binding.layoutContent.alpha = 0f
                    binding.layoutContent.translationY = 48f
                    binding.layoutContent.visibility = View.VISIBLE
                    binding.layoutContent.animate()
                        .alpha(1f).translationY(0f)
                        .setDuration(380)
                        .setInterpolator(android.view.animation.DecelerateInterpolator(2f))
                        .start()
                } else {
                    binding.layoutContent.visibility = View.VISIBLE
                }
                binding.layoutSwipeHint.visibility = if (items.size >= 2) View.VISIBLE else View.GONE
                adapter.submitList(items.toList())
                binding.textRestaurantName.text = items.firstOrNull()?.restaurantName ?: ""
                updateTotals()
                updateDeliveryProgress()
            }
        }

        binding.btnClearCart.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Vaciar carrito")
                .setMessage("¿Eliminar todos los platillos del carrito?")
                .setPositiveButton("Vaciar") { _, _ -> CartRepository.clearCart() }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        CartRepository.promoDiscount.observe(viewLifecycleOwner) {
            updateTotals()
            updateDeliveryProgress()
        }

        binding.btnApplyPromo.setOnClickListener {
            val code = binding.editPromoCode.text?.toString() ?: ""
            if (CartRepository.applyPromo(code)) {
                Toast.makeText(requireContext(), "Código aplicado exitosamente", Toast.LENGTH_SHORT).show()
                binding.editPromoCode.clearFocus()
            } else {
                Toast.makeText(requireContext(), "Código inválido. Prueba: MANDADITO20", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCheckout.setOnClickListener {
            when {
                !UserAuthManager.isLoggedIn() -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Inicia sesión")
                        .setMessage("Necesitas una cuenta para realizar pedidos. ¿Deseas iniciar sesión?")
                        .setPositiveButton("Iniciar sesión") { _, _ ->
                            startActivity(Intent(requireContext(), LoginActivity::class.java))
                            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
                UserAuthManager.isBlocked() -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Cuenta bloqueada")
                        .setMessage("Tu cuenta ha sido bloqueada por sanciones acumuladas. Contacta a soporte para más información.")
                        .setPositiveButton("Entendido", null)
                        .show()
                }
                else -> showPaymentBottomSheet()
            }
        }
    }

    private fun setupSwipeToDelete() {
        val swipeBackground = ColorDrawable(Color.parseColor("#1A1A1A"))
        val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_close)
        val iconMargin = resources.getDimensionPixelSize(R.dimen.icon_margin_swipe)

        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val removedItem = adapter.currentList[pos]
                viewHolder.itemView.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                CartRepository.removeItem(removedItem)

                Snackbar.make(binding.root, "${removedItem.menuItem.name} eliminado", Snackbar.LENGTH_LONG)
                    .setAction("Deshacer") { CartRepository.addBack(removedItem) }
                    .setActionTextColor(Color.WHITE)
                    .setBackgroundTint(Color.parseColor("#1A1A1A"))
                    .show()
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                if (dX < 0) {
                    swipeBackground.setBounds(
                        itemView.right + dX.toInt(), itemView.top,
                        itemView.right, itemView.bottom
                    )
                    swipeBackground.draw(c)

                    deleteIcon?.let { icon ->
                        val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                        val iconRight = itemView.right - iconMargin
                        val iconLeft = iconRight - icon.intrinsicWidth
                        icon.setBounds(iconLeft, iconTop, iconRight, iconTop + icon.intrinsicHeight)
                        icon.setTint(Color.WHITE)
                        icon.draw(c)
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerCart)
    }

    private fun showPaymentBottomSheet() {
        val items = CartRepository.items.value
        if (items.isNullOrEmpty()) return

        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val sheetBinding = BottomSheetPaymentBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        sheetBinding.textPaymentTotal.text = "$${CartRepository.total()}"
        sheetBinding.textDeliveryAddress.text = AddressManager.getSelectedLabel()

        // Populate items breakdown
        val d = resources.displayMetrics.density
        sheetBinding.layoutOrderItemsBreakdown.removeAllViews()
        items.forEach { cartItem ->
            val row = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, (6 * d).toInt(), 0, (6 * d).toInt())
            }
            row.addView(android.widget.TextView(requireContext()).apply {
                text = "${cartItem.menuItem.emoji} ${cartItem.menuItem.name}"
                textSize = 13f
                setTextColor(Color.parseColor("#1A1A1A"))
                layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            row.addView(android.widget.TextView(requireContext()).apply {
                text = "×${cartItem.quantity}"
                textSize = 12f
                setTextColor(Color.parseColor("#9E9E9E"))
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginStart = (8 * d).toInt(); marginEnd = (12 * d).toInt() }
            })
            row.addView(android.widget.TextView(requireContext()).apply {
                text = "$${cartItem.totalPrice}"
                textSize = 13f
                setTextColor(Color.parseColor("#1A1A1A"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            })
            sheetBinding.layoutOrderItemsBreakdown.addView(row)
        }
        // Subtotal / delivery / total summary
        val divider = android.view.View(requireContext()).apply {
            setBackgroundColor(Color.parseColor("#F0F0F0"))
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, (1 * d).toInt()
            ).apply { topMargin = (8 * d).toInt(); bottomMargin = (8 * d).toInt() }
        }
        sheetBinding.layoutOrderItemsBreakdown.addView(divider)
        fun summaryRow(label: String, value: String, bold: Boolean = false) {
            val row2 = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (4 * d).toInt() }
            }
            row2.addView(android.widget.TextView(requireContext()).apply {
                text = label; textSize = 12f
                setTextColor(Color.parseColor("#6B6B6B"))
                layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            row2.addView(android.widget.TextView(requireContext()).apply {
                text = value; textSize = 12f
                setTextColor(if (bold) Color.parseColor("#1A1A1A") else Color.parseColor("#6B6B6B"))
                if (bold) typeface = android.graphics.Typeface.DEFAULT_BOLD
            })
            sheetBinding.layoutOrderItemsBreakdown.addView(row2)
        }
        summaryRow("Subtotal", "$${CartRepository.subtotal()}")
        val fee = CartRepository.deliveryFee()
        summaryRow("Envío", if (fee == 0) "Gratis" else "$${fee}")
        val disc = CartRepository.discountAmount()
        if (disc > 0) summaryRow("Descuento", "-$${disc}")
        summaryRow("Total", "$${CartRepository.total()}", bold = true)

        fun selectOption(option: String) {
            selectedPayment = option
            sheetBinding.checkCash.visibility = if (option == "cash") View.VISIBLE else View.GONE
            sheetBinding.checkCard.visibility = if (option == "card") View.VISIBLE else View.GONE
            sheetBinding.checkOxxo.visibility = if (option == "oxxo") View.VISIBLE else View.GONE
        }

        selectOption("cash")

        sheetBinding.optionCash.setOnClickListener { selectOption("cash") }
        sheetBinding.optionCard.setOnClickListener { selectOption("card") }
        sheetBinding.optionOxxo.setOnClickListener { selectOption("oxxo") }

        sheetBinding.btnClosePayment.setOnClickListener { dialog.dismiss() }

        sheetBinding.btnConfirmPayment.setOnClickListener {
            dialog.dismiss()
            placeOrder()
        }

        dialog.show()
    }

    private fun placeOrder() {
        val items = CartRepository.items.value ?: return
        val restaurantName = items.firstOrNull()?.restaurantName ?: ""
        val total = CartRepository.total()
        val itemCount = CartRepository.itemCount()
        val paymentLabel = when (selectedPayment) {
            "card" -> "Tarjeta"; "oxxo" -> "OXXO"; else -> "Efectivo"
        }
        val note = binding.editDeliveryNote.text?.toString()?.trim() ?: ""
        if (note.isNotEmpty()) {
            android.widget.Toast.makeText(requireContext(), "📝 Nota registrada", android.widget.Toast.LENGTH_SHORT).show()
        }

        OrderHistoryManager.saveOrder(restaurantName, total, itemCount, paymentLabel)

        val orderNumber = Random.nextInt(1, 99999)

        binding.btnCheckout.isEnabled = false
        binding.btnCheckout.text = "✓  Pedido confirmado"
        binding.btnCheckout.animate()
            .scaleX(1.06f).scaleY(1.06f).setDuration(130).withEndAction {
                binding.btnCheckout.animate().scaleX(1f).scaleY(1f).setDuration(110).withEndAction {
                    CartRepository.clearCart()
                    val intent = Intent(requireContext(), OrderTrackingActivity::class.java)
                    intent.putExtra("order_number", orderNumber)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }.start()
            }.start()
    }

    private fun startScooterFloat() {
        scooterAnimator = ObjectAnimator.ofFloat(binding.imgEmptyScooter, "translationY", 0f, -18f, 0f).apply {
            duration = 2400
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun updateTotals() {
        binding.textSubtotal.text = "$${CartRepository.subtotal()}"
        binding.textDelivery.text = if (CartRepository.deliveryFee() == 0) "Gratis" else "$${CartRepository.deliveryFee()}"
        binding.textTotal.text = "$${CartRepository.total()}"

        val discount = CartRepository.discountAmount()
        if (discount > 0) {
            binding.rowDiscount.visibility = View.VISIBLE
            binding.textDiscount.text = "-$$discount"
        } else {
            binding.rowDiscount.visibility = View.GONE
        }

        binding.btnCheckout.animate()
            .scaleX(1.04f).scaleY(1.04f).setDuration(110).withEndAction {
                binding.btnCheckout.animate().scaleX(1f).scaleY(1f).setDuration(140).start()
            }.start()
    }

    private fun updateDeliveryProgress() {
        val subtotal = CartRepository.subtotal()
        val remaining = CartRepository.remainingForFreeDelivery()
        val progress = minOf(subtotal, 200)

        binding.progressDelivery.progress = progress

        if (remaining == 0) {
            binding.textDeliveryProgressLabel.text = "¡Envío gratis activado!"
            binding.textDeliveryProgressPct.text = "100%"
            binding.textDeliveryProgressLabel.setTextColor(resources.getColor(R.color.success, null))
        } else {
            binding.textDeliveryProgressLabel.text = "Agrega \$$remaining más para envío gratis"
            binding.textDeliveryProgressPct.text = "${progress * 100 / 200}%"
            binding.textDeliveryProgressLabel.setTextColor(resources.getColor(R.color.brown_dark, null))
        }
    }

    override fun onResume() {
        super.onResume()
        val b = _binding ?: return
        if (CartRepository.items.value?.isNotEmpty() == true) {
            b.btnCheckout.isEnabled = true
            b.btnCheckout.text = "Realizar pedido"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scooterAnimator?.cancel()
        _binding = null
    }
}
