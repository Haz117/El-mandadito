package com.elmandadito.app.ui

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.elmandadito.app.R
import com.elmandadito.app.data.AddressManager
import com.elmandadito.app.data.CartRepository
import com.elmandadito.app.data.FavoritesManager
import com.elmandadito.app.data.OrderHistoryManager
import com.elmandadito.app.data.UserPrefsManager
import com.elmandadito.app.databinding.ActivityMainBinding
import com.elmandadito.app.compose.ComposeHomeFragment
import com.elmandadito.app.ui.cart.CartFragment
import com.elmandadito.app.ui.favorites.FavoritesFragment
import com.elmandadito.app.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FavoritesManager.init(this)
        OrderHistoryManager.init(this)
        AddressManager.init(this)
        UserPrefsManager.init(this)
        CartRepository.init(this)

        setupBottomNav()
        setupBackNavigation()
        observeCart()

        if (savedInstanceState == null) {
            if (intent.getBooleanExtra("open_cart", false)) {
                switchFragment(CartFragment())
                binding.bottomNav.selectedItemId = R.id.nav_cart
            } else {
                switchFragment(ComposeHomeFragment())
                binding.bottomNav.selectedItemId = R.id.nav_home
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("open_cart", false)) {
            switchFragment(CartFragment())
            binding.bottomNav.selectedItemId = R.id.nav_cart
        }
    }

    fun selectCartTab() {
        switchFragment(CartFragment())
        binding.bottomNav.selectedItemId = R.id.nav_cart
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home      -> switchFragment(ComposeHomeFragment())
                R.id.nav_favorites -> switchFragment(FavoritesFragment())
                R.id.nav_cart      -> switchFragment(CartFragment())
                R.id.nav_profile   -> switchFragment(ProfileFragment())
            }
            true
        }
        binding.bottomNav.setOnItemReselectedListener {
            val current = supportFragmentManager.findFragmentById(R.id.fragment_container)
            (current as? ScrollableToTop)?.scrollToTop()
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val current = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (current !is ComposeHomeFragment) {
                    switchFragment(ComposeHomeFragment())
                    binding.bottomNav.selectedItemId = R.id.nav_home
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun observeCart() {
        CartRepository.items.observe(this) { items ->
            val count = items.sumOf { it.quantity }
            val badge = binding.bottomNav.getOrCreateBadge(R.id.nav_cart)
            badge.isVisible = count > 0
            if (count > 0) badge.number = count
        }
    }

    fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
