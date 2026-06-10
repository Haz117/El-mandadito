package com.elmandadito.app.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elmandadito.app.data.BusinessRepository
import com.elmandadito.app.data.OrderRecord
import com.elmandadito.app.data.SampleData
import com.elmandadito.app.databinding.ItemOrderHistoryBinding

class OrderHistoryAdapter(
    private val orders: List<OrderRecord>,
    private val onTap: ((OrderRecord) -> Unit)? = null
) : RecyclerView.Adapter<OrderHistoryAdapter.VH>() {

    inner class VH(val b: ItemOrderHistoryBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemOrderHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = orders.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val o = orders[position]
        with(holder.b) {
            val ctx = root.context
            BusinessRepository.init(ctx)
            val allRestaurants = SampleData.restaurants + BusinessRepository.getAll().map { it.toRestaurant() }
            textOrderIconEmoji.text = allRestaurants.find { it.name == o.restaurantName }?.emoji ?: "🍽️"

            textOrderRestaurant.text = o.restaurantName
            textOrderDate.text = o.date
            textOrderTotal.text = "$${o.total}"
            textOrderItems.text = "${o.itemCount} items"
            textOrderPayment.text = o.paymentMethod

            if (o.ratingStars > 0) {
                textOrderRating.visibility = View.VISIBLE
                textOrderRating.text = "★".repeat(o.ratingStars) + "☆".repeat(5 - o.ratingStars)
            } else {
                textOrderRating.visibility = View.GONE
            }

            root.setOnClickListener { onTap?.invoke(o) }
        }
    }
}
