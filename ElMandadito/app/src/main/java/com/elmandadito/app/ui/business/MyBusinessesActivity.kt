package com.elmandadito.app.ui.business

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.elmandadito.app.R
import com.elmandadito.app.data.BusinessData
import com.elmandadito.app.data.BusinessRepository
import com.elmandadito.app.data.UserPrefsManager
import com.elmandadito.app.databinding.ActivityMyBusinessesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MyBusinessesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyBusinessesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyBusinessesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UserPrefsManager.init(this)
        BusinessRepository.init(this)

        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.btnAddBusiness.setOnClickListener {
            startActivity(Intent(this, RegisterBusinessActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        val email = UserPrefsManager.getEmail()
        val businesses = BusinessRepository.getByOwner(email)
        val d = resources.displayMetrics.density

        binding.layoutBusinesses.removeAllViews()

        if (businesses.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.layoutBusinesses.visibility = View.GONE
            return
        }

        binding.layoutEmpty.visibility = View.GONE
        binding.layoutBusinesses.visibility = View.VISIBLE

        businesses.forEachIndexed { index, business ->
            val card = buildCard(business, d)
            binding.layoutBusinesses.addView(card)
            card.alpha = 0f
            card.translationY = 24f
            card.animate().alpha(1f).translationY(0f)
                .setStartDelay(index * 60L).setDuration(320)
                .setInterpolator(DecelerateInterpolator(2f)).start()
        }
    }

    private fun buildCard(b: BusinessData, d: Float): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f * d
                setColor(Color.WHITE)
                setStroke((1.5f * d).toInt(), Color.parseColor("#EBEBEB"))
            }
            setPadding((16 * d).toInt(), (14 * d).toInt(), (16 * d).toInt(), (14 * d).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (12 * d).toInt() }
        }

        // ── Row 1: emoji + name + open badge ──────────────────────────────
        val row1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (6 * d).toInt() }
        }

        row1.addView(android.widget.ImageView(this).apply {
            setImageResource(categoryIconRes(b.category))
            setColorFilter(Color.parseColor("#6B6B6B"))
            scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            setPadding((10 * d).toInt(), (10 * d).toInt(), (10 * d).toInt(), (10 * d).toInt())
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#F6F6F6"))
            }
            layoutParams = LinearLayout.LayoutParams((44 * d).toInt(), (44 * d).toInt())
                .apply { marginEnd = (10 * d).toInt() }
        })

        val nameCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        nameCol.addView(TextView(this).apply {
            text = b.name
            textSize = 15f
            setTextColor(Color.parseColor("#1A1A1A"))
            setTypeface(typeface, Typeface.BOLD)
        })
        nameCol.addView(TextView(this).apply {
            text = b.category.replaceFirstChar { it.uppercase() }
            textSize = 12f
            setTextColor(Color.parseColor("#6B6B6B"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = (2 * d).toInt() }
        })
        row1.addView(nameCol)

        row1.addView(TextView(this).apply {
            text = if (b.isOpen) "Abierto" else "Cerrado"
            textSize = 11f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding((10 * d).toInt(), (5 * d).toInt(), (10 * d).toInt(), (5 * d).toInt())
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f * d
                setColor(if (b.isOpen) Color.parseColor("#2E7D32") else Color.parseColor("#9E9E9E"))
            }
        })
        card.addView(row1)

        // ── Meta row ──────────────────────────────────────────────────────
        card.addView(TextView(this).apply {
            text = buildString {
                append(b.deliveryTime)
                append("  ·  Envío $${b.deliveryFee}")
                if (b.minimumOrder > 0) append("  ·  Mín. $${b.minimumOrder}")
                if (!b.phone.isNullOrBlank()) append("  ·  ${b.phone}")
            }
            textSize = 12f
            setTextColor(Color.parseColor("#6B6B6B"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (10 * d).toInt() }
        })

        // ── Products count ────────────────────────────────────────────────
        if (b.menuItems.isNotEmpty()) {
            card.addView(TextView(this).apply {
                text = "${b.menuItems.size} producto${if (b.menuItems.size != 1) "s" else ""} en el menú"
                textSize = 12f
                setTextColor(Color.parseColor("#9E9E9E"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (10 * d).toInt() }
            })
        }

        // ── Fake stats ────────────────────────────────────────────────────
        val seed = b.id.hashCode().toLong()
        val views = 50 + kotlin.math.abs(seed % 450).toInt()
        val orders = 5 + kotlin.math.abs((seed / 7) % 95).toInt()

        val statsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 12f * d
                setColor(Color.parseColor("#F6F6F6"))
            }
            setPadding((12 * d).toInt(), (10 * d).toInt(), (12 * d).toInt(), (10 * d).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (10 * d).toInt() }
        }
        fun statCell(label: String, value: String): LinearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            addView(TextView(this@MyBusinessesActivity).apply {
                text = value; textSize = 16f; gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#1A1A1A"))
                setTypeface(typeface, Typeface.BOLD)
            })
            addView(TextView(this@MyBusinessesActivity).apply {
                text = label; textSize = 11f; gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#9E9E9E"))
            })
        }
        fun vDivider() = View(this).apply {
            setBackgroundColor(Color.parseColor("#E0E0E0"))
            layoutParams = LinearLayout.LayoutParams((1 * d).toInt(), (28 * d).toInt())
        }
        statsRow.addView(statCell("Vistas este mes", views.toString()))
        statsRow.addView(vDivider())
        statsRow.addView(statCell("Pedidos este mes", orders.toString()))
        statsRow.addView(vDivider())
        statsRow.addView(statCell("Calificación", "5.0"))
        card.addView(statsRow)

        // ── Open/Closed toggle ────────────────────────────────────────────
        val toggleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (10 * d).toInt() }
        }
        toggleRow.addView(TextView(this).apply {
            text = "Estado del negocio"
            textSize = 13f
            setTextColor(Color.parseColor("#6B6B6B"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        val toggle = androidx.appcompat.widget.SwitchCompat(this).apply {
            isChecked = b.isOpen
            setOnCheckedChangeListener { _, isChecked ->
                BusinessRepository.save(b.copy(isOpen = isChecked))
                android.widget.Toast.makeText(
                    this@MyBusinessesActivity,
                    if (isChecked) "✓ ${b.name} marcado como abierto" else "${b.name} marcado como cerrado",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
        toggleRow.addView(toggle)
        card.addView(toggleRow)

        // ── Divider ───────────────────────────────────────────────────────
        card.addView(View(this).apply {
            setBackgroundColor(Color.parseColor("#F0F0F0"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (1f * d).toInt()
            ).apply { bottomMargin = (10 * d).toInt() }
        })

        // ── Action buttons ────────────────────────────────────────────────
        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        actions.addView(makeActionBtn("Editar",
            Color.parseColor("#F6F6F6"), Color.parseColor("#E0E0E0"), Color.parseColor("#1A1A1A"),
            d, marginEnd = (8 * d).toInt()
        ).apply {
            setOnClickListener {
                val intent = Intent(this@MyBusinessesActivity, RegisterBusinessActivity::class.java)
                intent.putExtra("edit_business_id", b.id)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        })

        actions.addView(makeActionBtn("Eliminar",
            Color.parseColor("#FFEBEE"), Color.parseColor("#FFCDD2"), Color.parseColor("#E53935"),
            d, marginEnd = (8 * d).toInt()
        ).apply {
            setOnClickListener {
                MaterialAlertDialogBuilder(this@MyBusinessesActivity)
                    .setTitle("Eliminar negocio")
                    .setMessage("¿Eliminar \"${b.name}\"? Esta acción no se puede deshacer.")
                    .setPositiveButton("Eliminar") { _, _ ->
                        BusinessRepository.delete(b.id)
                        refreshList()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        })

        actions.addView(makeActionBtn("Ver menú",
            Color.parseColor("#F6F6F6"), Color.parseColor("#E0E0E0"), Color.parseColor("#1A1A1A"),
            d, marginEnd = 0
        ).apply {
            setOnClickListener {
                val restaurantId = kotlin.math.abs(b.id.hashCode())
                val intent = Intent(this@MyBusinessesActivity,
                    com.elmandadito.app.ui.detail.RestaurantDetailActivity::class.java)
                intent.putExtra("restaurant_id", restaurantId)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        })

        card.addView(actions)
        return card
    }

    private fun categoryIconRes(category: String) = when (category.lowercase()) {
        "mexican", "mexicana"         -> R.drawable.ic_food_mexican
        "burgers", "hamburguesas"     -> R.drawable.ic_food_burger
        "pizza"                       -> R.drawable.ic_food_pizza
        "sushi"                       -> R.drawable.ic_food_sushi
        "chicken", "pollo"            -> R.drawable.ic_food_chicken
        "desserts", "postres", "cafe" -> R.drawable.ic_food_dessert
        else                          -> R.drawable.ic_food_mexican
    }

    private fun makeActionBtn(
        label: String, bgColor: Int, strokeColor: Int, textColor: Int,
        d: Float, marginEnd: Int
    ) = TextView(this).apply {
        text = label
        textSize = 13f
        setTextColor(textColor)
        gravity = Gravity.CENTER
        setPadding((14 * d).toInt(), (9 * d).toInt(), (14 * d).toInt(), (9 * d).toInt())
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 10f * d
            setColor(bgColor)
            setStroke((1f * d).toInt(), strokeColor)
        }
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { this.marginEnd = marginEnd }
        isClickable = true
        isFocusable = true
    }
}
