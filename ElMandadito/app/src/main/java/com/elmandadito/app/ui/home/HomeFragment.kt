package com.elmandadito.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        applyFilters()
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
            Toast.makeText(requireContext(), "Código $code aplicado al carrito", Toast.LENGTH_SHORT).show()
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

        sheetBinding.btnResetFilters.setOnClickListener {
            sortBy = SortBy.DEFAULT
            freeDeliveryOnly = false
            openOnly = false
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
            dialog.dismiss()
            applyFilters()
            updateFilterButtonState()
        }

        dialog.show()
    }

    private fun updateFilterButtonState() {
        val isActive = sortBy != SortBy.DEFAULT || freeDeliveryOnly || openOnly
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
        _binding = null
    }
}
