package com.elmandadito.app.compose

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.elmandadito.app.R
import com.elmandadito.app.data.Restaurant
import com.elmandadito.app.network.dto.toRestaurant
import com.elmandadito.app.ui.MainActivity
import com.elmandadito.app.ui.common.UiState
import com.elmandadito.app.ui.detail.RestaurantDetailActivity
import com.elmandadito.app.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComposeHomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val restaurantsState by homeViewModel.restaurants.collectAsState()

            val networkRestaurants = when (val state = restaurantsState) {
                is UiState.Success -> state.data.map { it.toRestaurant() }
                else -> emptyList()
            }

            MaterialTheme {
                HomeScreen(
                    networkRestaurants = networkRestaurants,
                    onRestaurantClick = { restaurant -> navigateToRestaurant(restaurant) },
                    onCartClick = { (activity as? MainActivity)?.selectCartTab() }
                )
            }
        }
    }

    private fun navigateToRestaurant(restaurant: Restaurant) {
        val intent = Intent(requireContext(), RestaurantDetailActivity::class.java)
        if (restaurant.networkId > 0L) {
            intent.putExtra("restaurant_id_long", restaurant.networkId)
        } else {
            intent.putExtra("restaurant_id", restaurant.id)
        }
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}
