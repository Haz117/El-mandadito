package com.elmandadito.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elmandadito.app.R
import com.elmandadito.app.data.FeaturedDeal
import com.elmandadito.app.databinding.ItemFeaturedDealBinding

class FeaturedAdapter(
    private val deals: List<FeaturedDeal>,
    private val onApplyCode: (String) -> Unit
) : RecyclerView.Adapter<FeaturedAdapter.VH>() {

    inner class VH(val b: ItemFeaturedDealBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemFeaturedDealBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = deals.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val deal = deals[position]
        with(holder.b) {
            val bgRes = when (deal.bgType) {
                0    -> R.drawable.bg_featured_red
                1    -> R.drawable.bg_featured_gold
                else -> R.drawable.bg_featured_dark
            }
            viewFeaturedBg.setBackgroundResource(bgRes)
            textFeaturedBadge.text = deal.badge
            textFeaturedTitle.text = deal.title
            textFeaturedSubtitle.text = deal.subtitle
            root.setOnClickListener {
                deal.promoCode?.let { code -> onApplyCode(code) }
            }
        }
    }
}
