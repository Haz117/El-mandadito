package com.elmandadito.app.ui.detail

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.elmandadito.app.R
import com.elmandadito.app.data.CartRepository
import com.elmandadito.app.data.MenuItem
import com.elmandadito.app.data.SampleData
import com.elmandadito.app.databinding.ActivityRestaurantDetailBinding
import com.elmandadito.app.databinding.BottomSheetAddItemBinding
import com.elmandadito.app.ui.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RestaurantDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRestaurantDetailBinding
    private lateinit var menuAdapter: MenuAdapter
    private var restaurantName = ""
    private var restaurantCategory = ""
    private var isFloatingCartVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val restaurantId = intent.getIntExtra("restaurant_id", -1)
        val restaurant = SampleData.restaurants.find { it.id == restaurantId } ?: run { finish(); return }
        restaurantName = restaurant.name
        restaurantCategory = restaurant.category

        binding.viewHeroBg.setBackgroundResource(categoryBg(restaurant.category))
        binding.imgHeroFood.setImageResource(categoryIcon(restaurant.category))
        binding.textRestaurantName.text = restaurant.name
        binding.textRating.text = restaurant.rating.toString()
        binding.textTime.text = restaurant.deliveryTime
        binding.textDeliveryFee.text = if (restaurant.deliveryFee == 0) "Gratis" else "$${restaurant.deliveryFee}"

        if (restaurant.isOpen) {
            binding.layoutDetailStatus.setBackgroundResource(R.drawable.bg_open_badge)
            binding.dotDetailStatus.setBackgroundColor(Color.parseColor("#2E7D32"))
            binding.textDetailStatus.text = "Abierto"
            binding.textDetailStatus.setTextColor(Color.parseColor("#2E7D32"))
        } else {
            binding.layoutDetailStatus.setBackgroundResource(R.drawable.bg_closed_badge)
            binding.dotDetailStatus.setBackgroundColor(Color.parseColor("#9E9E9E"))
            binding.textDetailStatus.text = "Cerrado"
            binding.textDetailStatus.setTextColor(Color.parseColor("#6B6B6B"))
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = restaurant.name
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        menuAdapter = MenuAdapter(restaurant.category) { menuItem ->
            showItemCustomizationSheet(menuItem)
        }
        binding.recyclerMenu.layoutManager = LinearLayoutManager(this)
        binding.recyclerMenu.adapter = menuAdapter
        menuAdapter.submitSections(restaurant.menu)

        animateHeroEntrance()
        setupCategoryTabs(restaurant.menu.map { it.name })

        binding.editMenuSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s?.toString() ?: ""
                menuAdapter.filter(q)
                binding.btnClearMenuSearch.visibility = if (q.isNotEmpty()) View.VISIBLE else View.GONE
                binding.textNoMenuResults.visibility = if (menuAdapter.isEmpty()) View.VISIBLE else View.GONE
                binding.categoryTabsScroll.visibility = if (q.isNotEmpty()) View.GONE else View.VISIBLE
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.btnClearMenuSearch.setOnClickListener { binding.editMenuSearch.setText("") }

        CartRepository.items.observe(this) { items ->
            val restaurantItems = items.filter { it.restaurantName == restaurant.name }
            val restaurantCount = restaurantItems.sumOf { it.quantity }
            if (restaurantCount > 0) {
                binding.textCartCountBadge.text = restaurantCount.toString()
                binding.textFloatingItems.text = "$restaurantCount platillo${if (restaurantCount > 1) "s" else ""}"
                binding.textFloatingTotal.text = "$${CartRepository.total()}"
                showFloatingCart()
            } else {
                hideFloatingCart()
            }
        }

        binding.layoutFloatingCart.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("open_cart", true)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun animateHeroEntrance() {
        val views = listOf<View>(
            binding.textRestaurantName, binding.layoutDetailStatus,
            binding.textRating, binding.textTime, binding.textDeliveryFee
        )
        views.forEachIndexed { i, view ->
            view.alpha = 0f
            view.translationY = 22f
            view.animate()
                .alpha(1f).translationY(0f)
                .setStartDelay(180L + i * 55L)
                .setDuration(350)
                .setInterpolator(DecelerateInterpolator(2f))
                .start()
        }
    }

    private fun setupCategoryTabs(categories: List<String>) {
        if (categories.size <= 1) return
        binding.categoryTabsScroll.visibility = View.VISIBLE

        val density = resources.displayMetrics.density
        categories.forEach { catName ->
            val tab = TextView(this).apply {
                text = catName
                textSize = 12.5f
                setTextColor(ContextCompat.getColor(this@RestaurantDetailActivity, R.color.brown_dark))
                setPadding((14 * density).toInt(), (8 * density).toInt(), (14 * density).toInt(), (8 * density).toInt())
                background = GradientDrawable().apply {
                    cornerRadius = 20 * density
                    setColor(ContextCompat.getColor(this@RestaurantDetailActivity, R.color.surface))
                    setStroke((1 * density).toInt(), ContextCompat.getColor(this@RestaurantDetailActivity, R.color.divider))
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.marginEnd = (8 * density).toInt() }
                setOnClickListener { scrollToCategory(catName) }
            }
            binding.categoryTabsContainer.addView(tab)
        }
    }

    private fun scrollToCategory(categoryName: String) {
        val positions = menuAdapter.getCategoryPositions()
        val pos = positions[categoryName] ?: return
        binding.recyclerMenu.post {
            val lm = binding.recyclerMenu.layoutManager as LinearLayoutManager
            val itemView = lm.findViewByPosition(pos)
            val scrollY = binding.recyclerMenu.top + (itemView?.top ?: 0)
            binding.nestedScroll.smoothScrollTo(0, scrollY)
        }
    }

    private fun showItemCustomizationSheet(menuItem: MenuItem) {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val sheetBinding = BottomSheetAddItemBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        sheetBinding.viewItemHeroBg.setBackgroundResource(categoryBg(restaurantCategory))
        sheetBinding.imgItemHero.setImageResource(categoryIcon(restaurantCategory))
        sheetBinding.textSheetItemName.text = menuItem.name
        sheetBinding.textSheetItemDesc.text = menuItem.description
        sheetBinding.badgePopularSheet.visibility = if (menuItem.isPopular) View.VISIBLE else View.GONE

        var quantity = 1
        fun updateButton(animate: Boolean = false) {
            val total = menuItem.price * quantity
            sheetBinding.textSheetQty.text = quantity.toString()
            sheetBinding.textSheetItemPrice.text = "$${menuItem.price}"
            sheetBinding.btnAddToCartSheet.text = "Agregar $quantity · $$total"
            if (animate) {
                sheetBinding.textSheetQty.animate()
                    .scaleX(1.45f).scaleY(1.45f).setDuration(85).withEndAction {
                        sheetBinding.textSheetQty.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                    }.start()
                sheetBinding.btnAddToCartSheet.animate()
                    .scaleX(1.03f).scaleY(1.03f).setDuration(80).withEndAction {
                        sheetBinding.btnAddToCartSheet.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    }.start()
            }
        }
        updateButton()

        sheetBinding.btnSheetDecrease.setOnClickListener {
            if (quantity > 1) {
                it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                quantity--; updateButton(animate = true)
            }
        }
        sheetBinding.btnSheetIncrease.setOnClickListener {
            if (quantity < 10) {
                it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                quantity++; updateButton(animate = true)
            }
        }

        sheetBinding.btnAddToCartSheet.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            dialog.dismiss()
            doAddToCart(menuItem, quantity)
        }

        dialog.show()
    }

    private fun doAddToCart(menuItem: MenuItem, quantity: Int) {
        val current = CartRepository.currentRestaurantName
        if (current != null && current != restaurantName) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Nuevo pedido")
                .setMessage("Tienes platillos de $current en tu carrito.\n¿Deseas vaciarlo y pedir de $restaurantName?")
                .setPositiveButton("Vaciar y pedir") { _, _ ->
                    CartRepository.clearCart()
                    repeat(quantity) { CartRepository.addItem(menuItem, restaurantName, restaurantCategory) }
                    showAddedFeedback(menuItem.name, quantity)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            repeat(quantity) { CartRepository.addItem(menuItem, restaurantName, restaurantCategory) }
            showAddedFeedback(menuItem.name, quantity)
        }
    }

    private fun showAddedFeedback(name: String, qty: Int) {
        val label = if (qty == 1) "$name agregado" else "$qty × $name agregados"
        binding.layoutFloatingCart.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.scale_up)
        )
        android.widget.Toast.makeText(this, label, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun showFloatingCart() {
        if (!isFloatingCartVisible) {
            isFloatingCartVisible = true
            binding.layoutFloatingCart.visibility = View.VISIBLE
            binding.layoutFloatingCart.animate().translationY(0f).alpha(1f).setDuration(280).start()
        }
    }

    private fun hideFloatingCart() {
        if (isFloatingCartVisible) {
            isFloatingCartVisible = false
            binding.layoutFloatingCart.animate()
                .translationY(120f).alpha(0f).setDuration(200)
                .withEndAction { binding.layoutFloatingCart.visibility = View.GONE }
                .start()
        }
    }

    private fun categoryBg(cat: String) = when (cat) {
        "mexican"  -> R.drawable.bg_category_mexican
        "burgers"  -> R.drawable.bg_category_burgers
        "pizza"    -> R.drawable.bg_category_pizza
        "sushi"    -> R.drawable.bg_category_sushi
        "chicken"  -> R.drawable.bg_category_chicken
        "desserts" -> R.drawable.bg_category_desserts
        else       -> R.drawable.bg_hero
    }

    private fun categoryIcon(cat: String) = when (cat) {
        "mexican"  -> R.drawable.ic_food_mexican
        "burgers"  -> R.drawable.ic_food_burger
        "pizza"    -> R.drawable.ic_food_pizza
        "sushi"    -> R.drawable.ic_food_sushi
        "chicken"  -> R.drawable.ic_food_chicken
        "desserts" -> R.drawable.ic_food_dessert
        else       -> R.drawable.ic_food_mexican
    }
}
