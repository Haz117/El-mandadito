package com.elmandadito.app.ui.favorites

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import com.elmandadito.app.data.CartRepository
import com.elmandadito.app.data.FavoritesManager
import com.elmandadito.app.data.Restaurant
import com.elmandadito.app.databinding.FragmentFavoritesBinding
import com.elmandadito.app.ui.detail.RestaurantDetailActivity
import com.elmandadito.app.ui.home.RestaurantAdapter

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RestaurantAdapter
    private var allFavorites: List<Restaurant> = emptyList()
    private var activeFilter = "Todos"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RestaurantAdapter { restaurant ->
            val intent = Intent(requireContext(), RestaurantDetailActivity::class.java)
            intent.putExtra("restaurant_id", restaurant.id)
            startActivity(intent)
            requireActivity().overridePendingTransition(
                com.elmandadito.app.R.anim.slide_in_right,
                com.elmandadito.app.R.anim.slide_out_left
            )
        }

        binding.recyclerFavorites.adapter = adapter
        binding.recyclerFavorites.layoutManager =
            androidx.recyclerview.widget.GridLayoutManager(requireContext(), 1)

        binding.textFavoritesCount.alpha = 0f
        binding.textFavoritesCount.translationY = 28f
        binding.textFavoritesCount.animate()
            .alpha(1f).translationY(0f)
            .setStartDelay(80).setDuration(360)
            .setInterpolator(DecelerateInterpolator(2f)).start()

        CartRepository.items.observe(viewLifecycleOwner) { items ->
            val counts = items.groupBy { it.restaurantName }
                .mapValues { (_, v) -> v.sumOf { it.quantity } }
            adapter.updateCartBadges(counts)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshFavorites()
        val items = CartRepository.items.value ?: emptyList()
        val counts = items.groupBy { it.restaurantName }
            .mapValues { (_, v) -> v.sumOf { it.quantity } }
        adapter.updateCartBadges(counts)
    }

    private fun refreshFavorites() {
        allFavorites = FavoritesManager.getFavoriteRestaurants()
        setupFilterChips()
        applyFilter(activeFilter)
    }

    private fun setupFilterChips() {
        val filters = listOf("Todos", "Abiertos", "Mejor calificados")
        val container = binding.layoutFavoritesFilterChips
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

        fun styleChips(active: String) {
            chips.forEachIndexed { i, chip ->
                val sel = filters[i] == active
                chip.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 20f * d
                    setColor(if (sel) Color.parseColor("#1A1A1A") else Color.parseColor("#F6F6F6"))
                    setStroke((1f * d).toInt(), if (sel) Color.parseColor("#1A1A1A") else Color.parseColor("#E0E0E0"))
                }
                chip.setTextColor(if (sel) Color.WHITE else Color.parseColor("#6B6B6B"))
            }
        }

        chips.forEachIndexed { i, chip ->
            chip.setOnClickListener {
                activeFilter = filters[i]
                styleChips(activeFilter)
                applyFilter(activeFilter)
            }
        }
        styleChips(activeFilter)
    }

    private fun applyFilter(filter: String) {
        val filtered = when (filter) {
            "Abiertos"          -> allFavorites.filter { it.isOpen }
            "Mejor calificados" -> allFavorites.sortedByDescending { it.rating }
            else                -> allFavorites
        }
        val count = allFavorites.size
        binding.textFavoritesCount.text =
            if (count == 1) "1 restaurante guardado" else "$count restaurantes guardados"

        if (allFavorites.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.recyclerFavorites.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.recyclerFavorites.visibility = View.VISIBLE
            adapter.submitList(filtered)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
