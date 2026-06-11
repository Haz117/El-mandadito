package com.elmandadito.app.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elmandadito.app.R
import com.elmandadito.app.data.BusinessRepository
import com.elmandadito.app.data.OrderRecord
import com.elmandadito.app.data.SampleData
import com.elmandadito.app.databinding.ItemOrderHistoryBinding

class OrderHistoryAdapter(
    private val orders: List<OrderRecord>,
    private val onTap: ((OrderRecord) -> Unit)? = null,
    private val onRate: ((OrderRecord) -> Unit)? = null
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
            val category = allRestaurants.find { it.name == o.restaurantName }?.category ?: ""
            imgOrderIcon.setImageResource(categoryIconRes(category))

            textOrderRestaurant.text = o.restaurantName
            textOrderDate.text = o.date
            textOrderTotal.text = "$${o.total}"
            textOrderItems.text = "${o.itemCount} items"
            textOrderPayment.text = o.paymentMethod

            if (o.ratingStars > 0) {
                textOrderRating.visibility = View.VISIBLE
                textOrderRating.text = "${o.ratingStars}/5"
                textOrderRating.setOnClickListener(null)
            } else if (onRate != null) {
                textOrderRating.visibility = View.VISIBLE
                textOrderRating.text = "Calificar"
                textOrderRating.setOnClickListener { onRate.invoke(o) }
            } else {
                textOrderRating.visibility = View.GONE
            }

            root.setOnClickListener { onTap?.invoke(o) }
        }
    }

    private fun categoryIconRes(category: String) = when (category.lowercase()) {
        "mexican"  -> R.drawable.ic_food_mexican
        "burgers"  -> R.drawable.ic_food_burger
        "pizza"    -> R.drawable.ic_food_pizza
        "sushi"    -> R.drawable.ic_food_sushi
        "chicken"  -> R.drawable.ic_food_chicken
        "desserts" -> R.drawable.ic_food_dessert
        else       -> R.drawable.ic_food_mexican
    }
}
