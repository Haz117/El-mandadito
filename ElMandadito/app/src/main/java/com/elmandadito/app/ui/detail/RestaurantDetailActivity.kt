package com.elmandadito.app.ui.detail

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.elmandadito.app.R
import com.elmandadito.app.data.BusinessRepository
import com.elmandadito.app.data.CartRepository
import com.elmandadito.app.data.MenuItem
import com.elmandadito.app.data.SampleData
import com.elmandadito.app.databinding.ActivityRestaurantDetailBinding
import com.elmandadito.app.databinding.BottomSheetAddItemBinding
import com.elmandadito.app.network.dto.toMenuCategories
import com.elmandadito.app.network.dto.toRestaurant
import com.elmandadito.app.ui.MainActivity
import com.elmandadito.app.ui.common.UiState
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RestaurantDetailActivity : AppCompatActivity() {

    private val viewModel: RestaurantDetailViewModel by viewModels()

    private lateinit var binding: ActivityRestaurantDetailBinding
    private lateinit var menuAdapter: MenuAdapter
    private var restaurantName = ""
    private var restaurantCategory = ""
    private var isRestaurantOpen = true
    private var isFloatingCartVisible = false
    private var networkId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        networkId = intent.getLongExtra("restaurant_id_long", 0L)
        val localId = intent.getIntExtra("restaurant_id", -1)

        BusinessRepository.init(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        menuAdapter = MenuAdapter("other") { menuItem -> showItemCustomizationSheet(menuItem) }
        binding.recyclerMenu.layoutManager = LinearLayoutManager(this)
        binding.recyclerMenu.adapter = menuAdapter

        if (networkId > 0L) {
            // Load from backend
            viewModel.load(networkId)
            observeViewModel()
        } else {
            // Load from local SampleData / BusinessRepository
            val restaurant = SampleData.restaurants.find { it.id == localId }
                ?: BusinessRepository.getAll().map { it.toRestaurant() }.find { it.id == localId }
                ?: run { finish(); return }
            populateUi(
                name = restaurant.name,
                category = restaurant.category,
                rating = restaurant.rating.toString(),
                time = restaurant.deliveryTime,
                fee = if (restaurant.deliveryFee == 0) "Gratis" else "$${restaurant.deliveryFee}",
                isOpen = restaurant.isOpen,
                menu = restaurant.menu.map { it.name }
            )
            restaurantName = restaurant.name
            restaurantCategory = restaurant.category
            isRestaurantOpen = restaurant.isOpen
            menuAdapter = MenuAdapter(restaurant.category) { menuItem -> showItemCustomizationSheet(menuItem) }
            binding.recyclerMenu.adapter = menuAdapter
            menuAdapter.submitSections(restaurant.menu)
        }

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
            val restaurantItems = items.filter { it.restaurantName == restaurantName }
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

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.restaurant.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                val r = state.data
                                restaurantName = r.name
                                restaurantCategory = r.category?.lowercase() ?: "other"
                                isRestaurantOpen = r.isOpen
                                populateUi(
                                    name = r.name,
                                    category = restaurantCategory,
                                    rating = String.format("%.1f", r.rating),
                                    time = "${r.deliveryTimeMin}–${r.deliveryTimeMax} min",
                                    fee = if (r.deliveryFee == 0.0) "Gratis" else "$${r.deliveryFee.toInt()}",
                                    isOpen = r.isOpen,
                                    menu = emptyList()
                                )
                                menuAdapter = MenuAdapter(restaurantCategory) { menuItem ->
                                    showItemCustomizationSheet(menuItem)
                                }
                                binding.recyclerMenu.adapter = menuAdapter
                            }
                            is UiState.Error -> {
                                com.google.android.material.snackbar.Snackbar
                                    .make(binding.root, state.message, com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Reintentar") { viewModel.load(networkId) }
                                    .setBackgroundTint(Color.parseColor("#1A1A1A"))
                                    .setTextColor(Color.WHITE)
                                    .setActionTextColor(Color.parseColor("#FF9500"))
                                    .show()
                            }
                            else -> {}
                        }
                    }
                }
                launch {
                    viewModel.menu.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                val categories = state.data.toMenuCategories()
                                menuAdapter.submitSections(categories)
                                setupCategoryTabs(categories.map { it.name })
                            }
                            is UiState.Error -> {
                                com.google.android.material.snackbar.Snackbar
                                    .make(binding.root, "Error al cargar el menú. Toca para reintentar.", com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                                    .setAction("Reintentar") { viewModel.load(networkId) }
                                    .setBackgroundTint(Color.parseColor("#1A1A1A"))
                                    .setTextColor(Color.WHITE)
                                    .setActionTextColor(Color.parseColor("#FF9500"))
                                    .show()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun populateUi(
        name: String, category: String, rating: String,
        time: String, fee: String, isOpen: Boolean, menu: List<String>
    ) {
        binding.viewHeroBg.setBackgroundResource(categoryBg(category))
        binding.imgHeroFood.setImageResource(categoryIcon(category))
        binding.textRestaurantName.text = name
        binding.textRating.text = rating
        binding.textTime.text = time
        binding.textDeliveryFee.text = fee
        supportActionBar?.title = name

        if (isOpen) {
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

        animateHeroEntrance()
        if (menu.isNotEmpty()) setupCategoryTabs(menu)
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
        if (!isRestaurantOpen) {
            com.google.android.material.snackbar.Snackbar.make(binding.root, "Este negocio está cerrado por ahora", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                .setBackgroundTint(android.graphics.Color.parseColor("#1A1A1A"))
                .setTextColor(android.graphics.Color.WHITE)
                .show()
            return
        }
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
                    showAddedFeedback(menuItem, quantity)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            repeat(quantity) { CartRepository.addItem(menuItem, restaurantName, restaurantCategory) }
            showAddedFeedback(menuItem, quantity)
        }
    }

    private fun showAddedFeedback(menuItem: MenuItem, qty: Int) {
        val label = if (qty == 1) "${menuItem.name} agregado" else "$qty × ${menuItem.name} agregados"
        binding.layoutFloatingCart.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.scale_up)
        )
        launchEmojiFly(menuItem.emoji)
        com.google.android.material.snackbar.Snackbar.make(binding.root, label, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
            .setBackgroundTint(android.graphics.Color.parseColor("#1A1A1A"))
            .setTextColor(android.graphics.Color.WHITE)
            .show()
    }

    private fun launchEmojiFly(emoji: String) {
        val tv = android.widget.TextView(this).apply {
            text = emoji; textSize = 30f
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val root = window.decorView as android.widget.FrameLayout
        root.addView(tv)
        tv.post {
            tv.x = (root.width / 2 - tv.width / 2).toFloat()
            tv.y = root.height * 0.55f
            tv.animate()
                .x(root.width - 120f).y(root.height * 0.87f)
                .scaleX(0.2f).scaleY(0.2f).alpha(0f)
                .setDuration(500)
                .setInterpolator(android.view.animation.AccelerateInterpolator(1.4f))
                .withEndAction { root.removeView(tv) }
                .start()
        }
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_restaurant_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == R.id.action_info) { showInfoSheet(); return true }
        return super.onOptionsItemSelected(item)
    }

    private fun showInfoSheet() {
        val restaurant = SampleData.restaurants.find { it.name == restaurantName }
            ?: BusinessRepository.getAll().map { it.toRestaurant() }.find { it.name == restaurantName }
            ?: return

        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val d = resources.displayMetrics.density

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(0, (8 * d).toInt(), 0, (24 * d).toInt())
        }

        // Handle
        root.addView(android.view.View(this).apply {
            setBackgroundColor(Color.parseColor("#E0E0E0"))
            layoutParams = LinearLayout.LayoutParams((44 * d).toInt(), (4 * d).toInt())
                .apply { gravity = Gravity.CENTER_HORIZONTAL; topMargin = (8 * d).toInt(); bottomMargin = (20 * d).toInt() }
        })

        // Title row
        val titleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding((20 * d).toInt(), 0, (20 * d).toInt(), (16 * d).toInt())
        }
        titleRow.addView(TextView(this).apply {
            text = restaurant.emoji; textSize = 32f; gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams((52 * d).toInt(), (52 * d).toInt())
                .apply { marginEnd = (14 * d).toInt() }
        })
        val nameCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        nameCol.addView(TextView(this).apply {
            text = restaurant.name; textSize = 18f
            setTextColor(Color.parseColor("#1A1A1A")); setTypeface(typeface, Typeface.BOLD)
        })
        nameCol.addView(TextView(this).apply {
            text = restaurant.category.replaceFirstChar { it.uppercase() }
            textSize = 13f; setTextColor(Color.parseColor("#6B6B6B"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = (2 * d).toInt() }
        })
        titleRow.addView(nameCol)
        root.addView(titleRow)

        // Divider
        root.addView(android.view.View(this).apply {
            setBackgroundColor(Color.parseColor("#F0F0F0"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (1f * d).toInt()
            ).apply { bottomMargin = (16 * d).toInt() }
        })

        // Info rows
        fun infoRow(iconRes: Int, label: String, value: String) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding((20 * d).toInt(), (10 * d).toInt(), (20 * d).toInt(), (10 * d).toInt())
            }
            row.addView(android.widget.ImageView(this).apply {
                setImageResource(iconRes)
                setColorFilter(android.graphics.Color.parseColor("#6B6B6B"))
                layoutParams = LinearLayout.LayoutParams((18 * d).toInt(), (18 * d).toInt())
                    .apply { marginEnd = (12 * d).toInt() }
            })
            row.addView(TextView(this).apply {
                text = label; textSize = 13f; setTextColor(Color.parseColor("#6B6B6B"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            row.addView(TextView(this).apply {
                text = value; textSize = 13f
                setTextColor(Color.parseColor("#1A1A1A")); setTypeface(typeface, Typeface.BOLD)
            })
            root.addView(row)
        }

        infoRow(R.drawable.ic_star_filled,    "Calificación",       "${restaurant.rating} / 5.0")
        infoRow(R.drawable.ic_clock,          "Tiempo de entrega",  restaurant.deliveryTime)
        infoRow(R.drawable.ic_motor,          "Costo de envío",     if (restaurant.deliveryFee == 0) "Gratis" else "\$${restaurant.deliveryFee}")
        infoRow(R.drawable.ic_receipt,        "Pedido mínimo",      "\$${restaurant.minimumOrder}")
        infoRow(R.drawable.ic_check_circle,   "Estado",             if (restaurant.isOpen) "Abierto ahora" else "Cerrado")

        dialog.setContentView(root)
        dialog.show()
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
