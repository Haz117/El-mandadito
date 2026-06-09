package com.elmandadito.app.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elmandadito.app.R
import com.elmandadito.app.data.FavoritesManager
import com.elmandadito.app.data.Restaurant
import com.elmandadito.app.databinding.ItemRestaurantBinding

class RestaurantAdapter(
    private val onClick: (Restaurant) -> Unit
) : ListAdapter<Restaurant, RestaurantAdapter.VH>(DIFF) {

    private var cartCounts: Map<String, Int> = emptyMap()
    private val animatedPositions = mutableSetOf<Int>()

    override fun submitList(list: List<Restaurant>?) {
        animatedPositions.clear()
        super.submitList(list)
    }

    fun updateCartBadges(counts: Map<String, Int>) {
        val old = cartCounts
        cartCounts = counts
        currentList.forEachIndexed { index, restaurant ->
            if ((old[restaurant.name] ?: 0) != (counts[restaurant.name] ?: 0)) {
                notifyItemChanged(index)
            }
        }
    }

    inner class VH(val b: ItemRestaurantBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemRestaurantBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = getItem(position)
        with(holder.b) {
            viewGradient.setBackgroundResource(categoryBg(r.category))
            imgFood.setImageResource(categoryIcon(r.category))

            textName.text = r.name
            textTags.text = r.tags.joinToString("  ·  ")
            textRating.text = r.rating.toString()
            textTime.text = r.deliveryTime
            textDelivery.text = if (r.deliveryFee == 0) "Gratis" else "$${r.deliveryFee}"
            textMinimum.text = "Mín. $${r.minimumOrder}"

            if (r.isOpen) {
                layoutStatus.setBackgroundResource(R.drawable.bg_open_badge)
                dotStatus.setBackgroundResource(android.R.color.holo_green_dark)
                textStatus.text = "Abierto"
                textStatus.setTextColor(Color.parseColor("#2E7D32"))
                root.alpha = 1f
            } else {
                layoutStatus.setBackgroundResource(R.drawable.bg_closed_badge)
                dotStatus.setBackgroundColor(Color.parseColor("#9E9E9E"))
                textStatus.text = "Cerrado"
                textStatus.setTextColor(Color.parseColor("#6B6B6B"))
                root.alpha = 0.65f
            }

            if (r.promo != null) {
                layoutPromo.visibility = View.VISIBLE
                textPromo.text = r.promo
            } else {
                layoutPromo.visibility = View.GONE
            }

            // Favorite heart
            val isFav = FavoritesManager.isFavorite(r.id)
            btnFavorite.setImageResource(
                if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )
            btnFavorite.setOnClickListener {
                val nowFav = FavoritesManager.toggleFavorite(r.id)
                btnFavorite.setImageResource(
                    if (nowFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                )
                val anim = AnimationUtils.loadAnimation(it.context, R.anim.scale_up)
                btnFavorite.startAnimation(anim)
            }

            val cartCount = cartCounts[r.name] ?: 0
            if (cartCount > 0) {
                badgeCartCount.visibility = View.VISIBLE
                badgeCartCount.text = cartCount.toString()
            } else {
                badgeCartCount.visibility = View.GONE
            }

            root.setOnClickListener { if (r.isOpen) onClick(r) }
        }

        if (animatedPositions.add(position)) {
            holder.itemView.alpha = 0f
            holder.itemView.translationY = 44f
            holder.itemView.animate()
                .alpha(1f).translationY(0f)
                .setStartDelay((position * 55L).coerceAtMost(280L))
                .setDuration(360)
                .setInterpolator(android.view.animation.DecelerateInterpolator(2f))
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

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Restaurant>() {
            override fun areItemsTheSame(a: Restaurant, b: Restaurant) = a.id == b.id
            override fun areContentsTheSame(a: Restaurant, b: Restaurant) = a == b
        }
    }
}
