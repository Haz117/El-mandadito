package com.elmandadito.app.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.elmandadito.app.R
import com.elmandadito.app.data.AddressManager
import com.elmandadito.app.data.BusinessRepository
import com.elmandadito.app.data.CartRepository
import com.elmandadito.app.data.FavoritesManager
import com.elmandadito.app.data.OrderHistoryManager
import com.elmandadito.app.data.UserPrefsManager
import com.elmandadito.app.databinding.ActivityMainBinding
import com.elmandadito.app.compose.ComposeHomeFragment
import com.elmandadito.app.network.NetworkMonitor
import com.elmandadito.app.network.SessionManager
import com.elmandadito.app.network.repository.AuthRepository
import com.elmandadito.app.ui.auth.LoginActivity
import javax.inject.Inject
import com.elmandadito.app.ui.cart.CartFragment
import com.elmandadito.app.ui.favorites.FavoritesFragment
import com.elmandadito.app.ui.profile.ProfileFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var authRepository: AuthRepository
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
        BusinessRepository.init(this)

        setupBottomNav()
        setupBackNavigation()
        observeCart()
        observeNetworkState()
        observeSessionState()

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

    private fun bounceNavItem(itemId: Int) {
        val menuView = binding.bottomNav.getChildAt(0) as? android.view.ViewGroup ?: return
        val index = listOf(R.id.nav_home, R.id.nav_favorites, R.id.nav_cart, R.id.nav_profile).indexOf(itemId)
        if (index < 0 || index >= menuView.childCount) return
        val view = menuView.getChildAt(index)
        view.animate().scaleX(1.22f).scaleY(1.22f).setDuration(100)
            .withEndAction { view.animate().scaleX(1f).scaleY(1f).setDuration(150).start() }
            .start()
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            bounceNavItem(item.itemId)
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

    private var prevCartCount = 0

    private fun observeCart() {
        CartRepository.items.observe(this) { items ->
            val count = items.sumOf { it.quantity }
            val badge = binding.bottomNav.getOrCreateBadge(R.id.nav_cart)
            badge.isVisible = count > 0
            if (count > 0) badge.number = count
            if (count > prevCartCount) bounceNavItem(R.id.nav_cart)
            prevCartCount = count
        }
    }

    private fun observeNetworkState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                NetworkMonitor.isOnline.collect { isOnline ->
                    if (isOnline) {
                        if (binding.bannerOffline.visibility == View.VISIBLE) {
                            binding.bannerOffline.animate().alpha(0f).setDuration(220).withEndAction {
                                binding.bannerOffline.visibility = View.GONE
                            }.start()
                        }
                    } else {
                        binding.bannerOffline.alpha = 0f
                        binding.bannerOffline.visibility = View.VISIBLE
                        binding.bannerOffline.animate().alpha(1f).setDuration(280).start()
                    }
                }
            }
        }
    }

    private fun observeSessionState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                SessionManager.sessionExpired.collect { expired ->
                    if (expired) {
                        SessionManager.reset()
                        // Limpia ambas capas de sesión para que el Splash redirija correctamente
                        authRepository.logout()
                        UserPrefsManager.setLoggedIn(false)
                        CartRepository.clearCart()
                        Snackbar.make(binding.root, "Tu sesión ha expirado. Inicia sesión de nuevo.", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.parseColor("#C62828"))
                            .setTextColor(Color.WHITE)
                            .show()
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }
                }
            }
        }
    }

    fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
