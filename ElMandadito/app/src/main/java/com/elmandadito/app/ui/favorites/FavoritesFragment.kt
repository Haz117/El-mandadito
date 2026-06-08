package com.elmandadito.app.ui.favorites

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.elmandadito.app.data.CartRepository
import com.elmandadito.app.data.FavoritesManager
import com.elmandadito.app.databinding.FragmentFavoritesBinding
import com.elmandadito.app.ui.detail.RestaurantDetailActivity
import com.elmandadito.app.ui.home.RestaurantAdapter

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RestaurantAdapter

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
        val favorites = FavoritesManager.getFavoriteRestaurants()
        val count = favorites.size

        binding.textFavoritesCount.text = if (count == 1) "1 restaurante guardado" else "$count restaurantes guardados"

        if (favorites.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.recyclerFavorites.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.recyclerFavorites.visibility = View.VISIBLE
            adapter.submitList(favorites)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
