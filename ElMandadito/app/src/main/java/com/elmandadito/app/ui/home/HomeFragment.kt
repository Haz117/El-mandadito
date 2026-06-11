package com.elmandadito.app.ui.home

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.elmandadito.app.R
import com.elmandadito.app.data.AddressManager
import com.elmandadito.app.data.CartRepository
import com.elmandadito.app.data.FeaturedDeal
import com.elmandadito.app.data.Restaurant
import com.elmandadito.app.data.SampleData
import com.elmandadito.app.databinding.BottomSheetSortFilterBinding
import com.elmandadito.app.databinding.FragmentHomeBinding
import com.elmandadito.app.ui.MainActivity
import com.elmandadito.app.ui.ScrollableToTop
import com.elmandadito.app.ui.detail.RestaurantDetailActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar

class HomeFragment : Fragment(), ScrollableToTop {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var restaurantAdapter: RestaurantAdapter
    private val allRestaurants = SampleData.restaurants
    private var activeCategory = "all"
    private var searchQuery = ""

    private enum class SortBy { DEFAULT, RATING, TIME, FEE }
    private var sortBy = SortBy.DEFAULT
    private var freeDeliveryOnly = false
    private var openOnly = false
    private var minRating = 0.0
    private var maxDeliveryMin = 999
    private var skeletonAnimator: ObjectAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGreeting()
        setupFeaturedCarousel()
        setupRestaurantList()
        setupSearch()
        setupCategoryChips()
        setupSortFilter()
        setupPullToRefresh()
        setupHeaderCart()
        showSkeletonThenLoad()
    }

    override fun scrollToTop() {
        binding.nestedScroll.smoothScrollTo(0, 0)
    }

    override fun onResume() {
        super.onResume()
        binding.textDeliveryAddressHeader.text = AddressManager.getSelectedLabel()
        val items = CartRepository.items.value ?: emptyList()
        val counts = items.groupBy { it.restaurantName }
            .mapValues { (_, v) -> v.sumOf { it.quantity } }
        restaurantAdapter.updateCartBadges(counts)
    }

    private fun setupHeaderCart() {
        binding.textDeliveryAddressHeader.text = AddressManager.getSelectedLabel()

        binding.btnHeaderCart.setOnClickListener {
            (requireActivity() as? MainActivity)?.selectCartTab()
        }

        CartRepository.items.observe(viewLifecycleOwner) { items ->
            val count = items.sumOf { it.quantity }
            binding.badgeHeaderCart.visibility = if (count > 0) View.VISIBLE else View.GONE
            binding.badgeHeaderCart.text = if (count > 9) "9+" else count.toString()
        }
    }

    private fun showSkeletonThenLoad() {
        binding.layoutSkeleton.visibility = View.VISIBLE
        binding.recyclerRestaurants.visibility = View.GONE
        skeletonAnimator = ObjectAnimator.ofFloat(binding.layoutSkeleton, "alpha", 0.4f, 1f).apply {
            duration = 700
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            if (_binding == null) return@postDelayed
            skeletonAnimator?.cancel()
            binding.layoutSkeleton.animate().alpha(0f).setDuration(200).withEndAction {
                if (_binding == null) return@withEndAction
                binding.layoutSkeleton.visibility = View.GONE
                binding.recyclerRestaurants.alpha = 0f
                binding.recyclerRestaurants.visibility = View.VISIBLE
                binding.recyclerRestaurants.animate().alpha(1f).setDuration(300).start()
                applyFilters()
            }.start()
        }, 700)
    }

    private fun setupGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Buenos días"
            hour < 19 -> "Buenas tardes"
            else      -> "Buenas noches"
        }
        binding.textGreeting.text = "$greeting, ¿qué antojo tienes?"
    }

    private fun setupFeaturedCarousel() {
        val deals = listOf(
            FeaturedDeal("20% OFF en tu primer pedido", "Usa el código MANDADITO20",
                "OFERTA ESPECIAL", "MANDADITO20", 0),
            FeaturedDeal("Envío gratis desde \$200", "Sin código necesario",
                "SIEMPRE ACTIVO", null, 1),
            FeaturedDeal("15% OFF con BIENVENIDO", "Para nuevos usuarios",
                "NUEVO USUARIO", "BIENVENIDO", 2)
        )
        val featuredAdapter = FeaturedAdapter(deals) { code ->
            CartRepository.applyPromo(code)
            Snackbar.make(binding.root, "Código $code aplicado al carrito", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(android.graphics.Color.parseColor("#1A1A1A"))
                .setTextColor(android.graphics.Color.WHITE)
                .show()
        }
        binding.recyclerFeatured.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerFeatured.adapter = featuredAdapter
    }

    private fun setupRestaurantList() {
        restaurantAdapter = RestaurantAdapter { openRestaurant(it) }
        binding.recyclerRestaurants.adapter = restaurantAdapter

        CartRepository.items.observe(viewLifecycleOwner) { items ->
            val counts = items.groupBy { it.restaurantName }
                .mapValues { (_, v) -> v.sumOf { it.quantity } }
            restaurantAdapter.updateCartBadges(counts)
        }
    }

    private fun setupSearch() {
        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString() ?: ""
                binding.btnClearSearch.visibility = if (searchQuery.isNotEmpty()) View.VISIBLE else View.GONE
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.btnClearSearch.setOnClickListener {
            binding.editSearch.setText("")
        }
    }

    private fun setupCategoryChips() {
        binding.chipAll.setOnCheckedChangeListener     { _, c -> if (c) { activeCategory = "all";      applyFilters() } }
        binding.chipMexican.setOnCheckedChangeListener { _, c -> if (c) { activeCategory = "mexican";  applyFilters() } }
        binding.chipBurgers.setOnCheckedChangeListener { _, c -> if (c) { activeCategory = "burgers";  applyFilters() } }
        binding.chipPizza.setOnCheckedChangeListener   { _, c -> if (c) { activeCategory = "pizza";    applyFilters() } }
        binding.chipSushi.setOnCheckedChangeListener   { _, c -> if (c) { activeCategory = "sushi";    applyFilters() } }
        binding.chipChicken.setOnCheckedChangeListener { _, c -> if (c) { activeCategory = "chicken";  applyFilters() } }
        binding.chipDesserts.setOnCheckedChangeListener{ _, c -> if (c) { activeCategory = "desserts"; applyFilters() } }
    }

    private fun setupSortFilter() {
        binding.btnSortFilter.setOnClickListener { showSortFilterSheet() }
    }

    private fun showSortFilterSheet() {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val sheetBinding = BottomSheetSortFilterBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        when (sortBy) {
            SortBy.DEFAULT -> sheetBinding.sortDefault.isChecked = true
            SortBy.RATING  -> sheetBinding.sortRating.isChecked = true
            SortBy.TIME    -> sheetBinding.sortTime.isChecked = true
            SortBy.FEE     -> sheetBinding.sortFee.isChecked = true
        }
        sheetBinding.switchFreeDelivery.isChecked = freeDeliveryOnly
        sheetBinding.switchOpenOnly.isChecked = openOnly

        when (minRating) {
            4.5  -> sheetBinding.chipRating45.isChecked = true
            4.0  -> sheetBinding.chipRating40.isChecked = true
            else -> sheetBinding.chipRatingAll.isChecked = true
        }
        when (maxDeliveryMin) {
            30   -> sheetBinding.chipTime30.isChecked = true
            45   -> sheetBinding.chipTime45.isChecked = true
            else -> sheetBinding.chipTimeAny.isChecked = true
        }

        sheetBinding.btnResetFilters.setOnClickListener {
            sortBy = SortBy.DEFAULT
            freeDeliveryOnly = false
            openOnly = false
            minRating = 0.0
            maxDeliveryMin = 999
            dialog.dismiss()
            applyFilters()
            updateFilterButtonState()
        }

        sheetBinding.btnApplyFilters.setOnClickListener {
            sortBy = when (sheetBinding.radioSort.checkedRadioButtonId) {
                R.id.sort_rating -> SortBy.RATING
                R.id.sort_time   -> SortBy.TIME
                R.id.sort_fee    -> SortBy.FEE
                else             -> SortBy.DEFAULT
            }
            freeDeliveryOnly = sheetBinding.switchFreeDelivery.isChecked
            openOnly = sheetBinding.switchOpenOnly.isChecked
            minRating = when (sheetBinding.chipGroupRating.checkedChipId) {
                R.id.chip_rating_45 -> 4.5
                R.id.chip_rating_40 -> 4.0
                else                -> 0.0
            }
            maxDeliveryMin = when (sheetBinding.chipGroupTime.checkedChipId) {
                R.id.chip_time_30 -> 30
                R.id.chip_time_45 -> 45
                else              -> 999
            }
            dialog.dismiss()
            applyFilters()
            updateFilterButtonState()
        }

        dialog.show()
    }

    private fun updateFilterButtonState() {
        val isActive = sortBy != SortBy.DEFAULT || freeDeliveryOnly || openOnly || minRating > 0 || maxDeliveryMin < 999
        binding.btnSortFilter.setBackgroundResource(
            if (isActive) R.drawable.bg_filter_btn_active else R.drawable.bg_filter_btn
        )
        binding.textSortFilter.setTextColor(
            if (isActive) resources.getColor(R.color.primary, null)
            else resources.getColor(R.color.brown_dark, null)
        )
        binding.imgSortFilter.setColorFilter(
            if (isActive) resources.getColor(R.color.primary, null)
            else resources.getColor(R.color.brown_dark, null)
        )
        binding.textActiveFilters.visibility = if (isActive) View.VISIBLE else View.GONE
    }

    private fun setupPullToRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            resources.getColor(R.color.primary, null),
            resources.getColor(R.color.accent, null)
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.textDeliveryAddressHeader.text = AddressManager.getSelectedLabel()
            applyFilters()
            restaurantAdapter.notifyDataSetChanged()
            binding.swipeRefresh.postDelayed({ binding.swipeRefresh.isRefreshing = false }, 800)
        }
    }

    private fun applyFilters() {
        var filtered = allRestaurants.toList()
        if (activeCategory != "all") filtered = filtered.filter { it.category == activeCategory }
        if (openOnly) filtered = filtered.filter { it.isOpen }
        if (freeDeliveryOnly) filtered = filtered.filter { it.deliveryFee == 0 }
        if (minRating > 0) filtered = filtered.filter { it.rating >= minRating }
        if (maxDeliveryMin < 999) {
            filtered = filtered.filter { r ->
                r.deliveryTime.split("-").firstOrNull()?.trim()
                    ?.replace("[^0-9]".toRegex(), "")?.toIntOrNull()?.let { it <= maxDeliveryMin } ?: true
            }
        }
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
            }
        }
        filtered = when (sortBy) {
            SortBy.RATING -> filtered.sortedByDescending { it.rating }
            SortBy.TIME   -> filtered.sortedBy {
                it.deliveryTime.split("-").firstOrNull()?.trim()?.replace("[^0-9]".toRegex(), "")?.toIntOrNull() ?: 99
            }
            SortBy.FEE    -> filtered.sortedBy { it.deliveryFee }
            SortBy.DEFAULT -> filtered
        }
        restaurantAdapter.submitList(filtered)
        binding.textNoResults.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.textResultCount.text = if (filtered.isNotEmpty()) "${filtered.size} restaurantes" else ""
    }

    private fun openRestaurant(restaurant: Restaurant) {
        val intent = Intent(requireContext(), RestaurantDetailActivity::class.java)
        intent.putExtra("restaurant_id", restaurant.id)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        skeletonAnimator?.cancel()
        _binding = null
    }
}
