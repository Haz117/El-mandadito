package com.elmandadito.app.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elmandadito.app.R
import com.elmandadito.app.data.CartItem
import com.elmandadito.app.databinding.ItemCartBinding

class CartAdapter(
    private val onIncrease: (Int) -> Unit,
    private val onDecrease: (Int) -> Unit
) : ListAdapter<CartItem, CartAdapter.VH>(DIFF) {

    inner class VH(val b: ItemCartBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int, payloads: List<Any>) {
        if (payloads.isNotEmpty()) {
            val item = getItem(position)
            with(holder.b) {
                textQty.text = item.quantity.toString()
                textTotalPrice.text = "$${item.totalPrice}"
                textQty.animate()
                    .scaleX(1.45f).scaleY(1.45f).setDuration(90).withEndAction {
                        textQty.animate().scaleX(1f).scaleY(1f).setDuration(130).start()
                    }.start()
            }
            return
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        with(holder.b) {
            imgCartFood.setImageResource(categoryIcon(item.restaurantCategory))
            viewCartIconBg.setBackgroundResource(categoryBg(item.restaurantCategory))
            textName.text = item.menuItem.name
            textUnitPrice.text = "$${item.menuItem.price} c/u"
            textTotalPrice.text = "$${item.totalPrice}"
            textQty.text = item.quantity.toString()
            btnIncrease.setOnClickListener {
                it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                onIncrease(item.menuItem.id)
            }
            btnDecrease.setOnClickListener {
                it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                onDecrease(item.menuItem.id)
            }
        }
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

    private fun categoryBg(cat: String) = when (cat) {
        "mexican"  -> R.drawable.bg_category_mexican
        "burgers"  -> R.drawable.bg_category_burgers
        "pizza"    -> R.drawable.bg_category_pizza
        "sushi"    -> R.drawable.bg_category_sushi
        "chicken"  -> R.drawable.bg_category_chicken
        "desserts" -> R.drawable.bg_category_desserts
        else       -> R.drawable.bg_category_mexican
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CartItem>() {
            override fun areItemsTheSame(a: CartItem, b: CartItem) = a.menuItem.id == b.menuItem.id
            override fun areContentsTheSame(a: CartItem, b: CartItem) =
                a.menuItem.id == b.menuItem.id && a.quantity == b.quantity
            override fun getChangePayload(oldItem: CartItem, newItem: CartItem): Any? =
                if (oldItem.quantity != newItem.quantity) "QTY" else null
        }
    }
}
