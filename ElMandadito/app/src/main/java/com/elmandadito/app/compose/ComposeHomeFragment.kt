package com.elmandadito.app.compose

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.elmandadito.app.R
import com.elmandadito.app.data.Restaurant
import com.elmandadito.app.ui.MainActivity
import com.elmandadito.app.ui.detail.RestaurantDetailActivity

class ComposeHomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme {
                HomeScreen(
                    onRestaurantClick = { restaurant -> navigateToRestaurant(restaurant) },
                    onCartClick = { (activity as? MainActivity)?.selectCartTab() }
                )
            }
        }
    }

    private fun navigateToRestaurant(restaurant: Restaurant) {
        val intent = Intent(requireContext(), RestaurantDetailActivity::class.java)
        intent.putExtra("restaurant_id", restaurant.id)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}
