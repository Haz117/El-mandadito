package com.elmandadito.app.ui.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elmandadito.app.R
import com.elmandadito.app.data.MenuCategory
import com.elmandadito.app.data.MenuItem
import com.elmandadito.app.databinding.ItemMenuCategoryBinding
import com.elmandadito.app.databinding.ItemMenuItemBinding

class MenuAdapter(
    private val category: String,
    private val onAdd: (MenuItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val rows = mutableListOf<Any>()
    private var allSections = listOf<MenuCategory>()

    fun submitSections(categories: List<MenuCategory>) {
        allSections = categories
        rebuildRows(categories)
    }

    fun filter(query: String) {
        if (query.isBlank()) { rebuildRows(allSections); return }
        val filtered = allSections.mapNotNull { cat ->
            val matches = cat.items.filter { it.name.contains(query, ignoreCase = true) }
            if (matches.isNotEmpty()) cat.copy(items = matches) else null
        }
        rebuildRows(filtered)
    }

    fun isEmpty() = rows.isEmpty()

    fun getCategoryPositions(): Map<String, Int> =
        rows.mapIndexedNotNull { index, row ->
            if (row is MenuCategory) row.name to index else null
        }.toMap()

    private fun rebuildRows(categories: List<MenuCategory>) {
        rows.clear()
        categories.forEach { cat -> rows.add(cat); rows.addAll(cat.items) }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = if (rows[position] is MenuCategory) 0 else 1
    override fun getItemCount() = rows.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == 0)
            CategoryVH(ItemMenuCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else
            ItemVH(ItemMenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryVH -> holder.bind(rows[position] as MenuCategory)
            is ItemVH     -> holder.bind(rows[position] as MenuItem)
        }
    }

    inner class CategoryVH(val b: ItemMenuCategoryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(cat: MenuCategory) { b.textCategoryName.text = cat.name }
    }

    inner class ItemVH(val b: ItemMenuItemBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: MenuItem) {
            b.viewIconBg.setBackgroundResource(categoryBg(category))
            b.imgFoodIcon.setImageResource(categoryIcon(category))
            b.textItemName.text = item.name
            b.textItemDesc.text = item.description
            b.textItemPrice.text = "$${item.price}"
            b.textPopularBadge.visibility = if (item.isPopular) View.VISIBLE else View.GONE
            b.btnAdd.setOnClickListener { onAdd(item) }
        }
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

    private fun categoryIcon(cat: String) = when (cat) {
        "mexican"  -> R.drawable.ic_food_mexican
        "burgers"  -> R.drawable.ic_food_burger
        "pizza"    -> R.drawable.ic_food_pizza
        "sushi"    -> R.drawable.ic_food_sushi
        "chicken"  -> R.drawable.ic_food_chicken
        "desserts" -> R.drawable.ic_food_dessert
        else       -> R.drawable.ic_food_mexican
    }
}
