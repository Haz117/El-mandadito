package com.elmandadito.app.ui.business

import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.elmandadito.app.R
import com.elmandadito.app.data.BusinessData
import com.elmandadito.app.data.BusinessRepository
import com.elmandadito.app.data.MenuItem
import com.elmandadito.app.data.UserPrefsManager
import com.elmandadito.app.databinding.ActivityRegisterBusinessBinding

class RegisterBusinessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBusinessBinding
    private var selectedImageUri: Uri? = null
    private var editBusinessId: String? = null  // non-null = edit mode

    private val categories = listOf(
        "mexican"  to "🌮 Tacos",
        "burgers"  to "🍔 Burgers",
        "pizza"    to "🍕 Pizza",
        "sushi"    to "🍣 Sushi",
        "chicken"  to "🍗 Pollo",
        "desserts" to "🍰 Postres",
        "other"    to "🍽️ Otro"
    )
    private var selectedCategory = "mexican"

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        selectedImageUri = uri
        runCatching {
            contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        binding.imgBizPreview.setImageURI(uri)
        binding.imgBizPreview.visibility = View.VISIBLE
        binding.layoutPickHint.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBusinessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UserPrefsManager.init(this)
        BusinessRepository.init(this)

        editBusinessId = intent.getStringExtra("edit_business_id")

        setupCategoryChips()

        editBusinessId?.let { id ->
            val existing = BusinessRepository.getAll().find { it.id == id }
            if (existing != null) populateForEdit(existing)
        }

        animateEntrance()

        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.layoutImagePicker.setOnClickListener { pickImage.launch("image/*") }
        binding.btnAddProduct.setOnClickListener { addMenuRow() }
        binding.btnRegisterBusiness.setOnClickListener { attemptRegister() }

        if (editBusinessId != null) {
            // Update title and button text for edit mode
            // (header TextView is not bound — update button)
            binding.btnRegisterBusiness.text = "Guardar cambios"
        }
    }

    private fun populateForEdit(b: BusinessData) {
        binding.editBizName.setText(b.name)
        binding.editBizEmoji.setText(b.emoji)
        binding.editBizTags.setText(b.tags.joinToString(", "))
        binding.editBizPromo.setText(b.promo ?: "")
        binding.editBizDeliveryTime.setText(b.deliveryTime)
        binding.editBizDeliveryFee.setText(if (b.deliveryFee > 0) b.deliveryFee.toString() else "")
        binding.editBizMinimum.setText(if (b.minimumOrder > 0) b.minimumOrder.toString() else "")
        binding.editBizPhone.setText(b.phone)
        binding.switchIsOpen.isChecked = b.isOpen

        selectedCategory = b.category
        refreshChipSelection()

        if (b.imageUri.isNotBlank()) {
            runCatching {
                binding.imgBizPreview.setImageURI(Uri.parse(b.imageUri))
                binding.imgBizPreview.visibility = View.VISIBLE
                binding.layoutPickHint.visibility = View.GONE
                selectedImageUri = Uri.parse(b.imageUri)
            }
        }

        b.menuItems.forEach { item ->
            addMenuRow(item.emoji, item.name, item.price.toString())
        }
    }

    private fun refreshChipSelection() {
        val container = binding.layoutCategories
        for (i in 0 until container.childCount) {
            val chip = container.getChildAt(i) as? TextView ?: continue
            setChipStyle(chip, chip.tag == selectedCategory)
        }
    }

    private fun setupCategoryChips() {
        val container = binding.layoutCategories
        container.removeAllViews()
        val d = resources.displayMetrics.density

        categories.forEach { (id, label) ->
            val chip = TextView(this).apply {
                text = label
                textSize = 13f
                setPadding((14 * d).toInt(), (8 * d).toInt(), (14 * d).toInt(), (8 * d).toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = (8 * d).toInt() }
                tag = id
                setChipStyle(this, id == selectedCategory)
                setOnClickListener {
                    selectedCategory = id
                    for (i in 0 until container.childCount) {
                        val c = container.getChildAt(i) as? TextView ?: continue
                        setChipStyle(c, c.tag == id)
                    }
                }
            }
            container.addView(chip)
        }
    }

    private fun setChipStyle(chip: TextView, selected: Boolean) {
        val d = resources.displayMetrics.density
        chip.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f * d
            if (selected) {
                setColor(Color.parseColor("#1A1A1A"))
                setStroke(0, Color.TRANSPARENT)
            } else {
                setColor(Color.parseColor("#FFFFFF"))
                setStroke((1f * d).toInt(), Color.parseColor("#E0E0E0"))
            }
        }
        chip.setTextColor(if (selected) Color.parseColor("#FFFFFF") else Color.parseColor("#6B6B6B"))
    }

    private fun addMenuRow(
        prefillEmoji: String = "",
        prefillName: String = "",
        prefillPrice: String = ""
    ) {
        val d = resources.displayMetrics.density

        fun editText(hint: String, weight: Float, inputType: Int = android.text.InputType.TYPE_CLASS_TEXT): EditText {
            return EditText(this).apply {
                this.hint = hint
                textSize = 13f
                setHintTextColor(Color.parseColor("#A0A0A0"))
                setTextColor(Color.parseColor("#1A1A1A"))
                background = null
                setSingleLine(true)
                this.inputType = inputType
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
            }
        }

        val emojiEdit = editText("Emoji", 0.6f).apply {
            textSize = 17f
            if (prefillEmoji.isNotEmpty()) setText(prefillEmoji)
        }
        val nameEdit = editText("Nombre del producto", 2f).apply {
            if (prefillName.isNotEmpty()) setText(prefillName)
        }
        val priceEdit = editText("$", 0.7f, android.text.InputType.TYPE_CLASS_NUMBER).apply {
            if (prefillPrice.isNotEmpty()) setText(prefillPrice)
        }

        val removeBtn = ImageView(this).apply {
            setImageResource(R.drawable.ic_close)
            setColorFilter(Color.parseColor("#BDBDBD"))
            val size = (36 * d).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size)
            setPadding((8 * d).toInt(), (8 * d).toInt(), (8 * d).toInt(), (8 * d).toInt())
            background = obtainStyledAttributes(
                intArrayOf(android.R.attr.selectableItemBackgroundBorderless)
            ).getDrawable(0)
            isClickable = true
            isFocusable = true
            contentDescription = "Eliminar"
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 14f * d
                setColor(Color.parseColor("#FFFFFF"))
                setStroke((1f * d).toInt(), Color.parseColor("#EBEBEB"))
            }
            val hPad = (14 * d).toInt()
            setPadding(hPad, 0, hPad, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (56 * d).toInt()
            ).apply { bottomMargin = (8 * d).toInt() }
        }

        container.addView(emojiEdit)
        container.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams((1 * d).toInt(), (28 * d).toInt())
                .apply { marginStart = (6 * d).toInt(); marginEnd = (6 * d).toInt() }
            setBackgroundColor(Color.parseColor("#EBEBEB"))
        })
        container.addView(nameEdit)
        container.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams((1 * d).toInt(), (28 * d).toInt())
                .apply { marginStart = (6 * d).toInt(); marginEnd = (6 * d).toInt() }
            setBackgroundColor(Color.parseColor("#EBEBEB"))
        })
        container.addView(priceEdit)
        container.addView(removeBtn)

        container.tag = Triple(emojiEdit, nameEdit, priceEdit)
        removeBtn.setOnClickListener { binding.layoutMenuItems.removeView(container) }

        binding.layoutMenuItems.addView(container)

        container.alpha = 0f
        container.translationY = 24f
        container.animate().alpha(1f).translationY(0f)
            .setDuration(280).setInterpolator(DecelerateInterpolator(2f)).start()
    }

    private fun attemptRegister() {
        val name = binding.editBizName.text?.toString()?.trim() ?: ""
        val emoji = binding.editBizEmoji.text?.toString()?.trim() ?: ""
        val tags = binding.editBizTags.text?.toString()?.trim() ?: ""
        val promo = binding.editBizPromo.text?.toString()?.trim() ?: ""
        val deliveryTime = binding.editBizDeliveryTime.text?.toString()?.trim() ?: ""
        val deliveryFeeStr = binding.editBizDeliveryFee.text?.toString()?.trim() ?: ""
        val minimumStr = binding.editBizMinimum.text?.toString()?.trim() ?: ""
        val phone = binding.editBizPhone.text?.toString()?.trim() ?: ""
        val isOpen = binding.switchIsOpen.isChecked

        if (name.isEmpty()) { showError("Escribe el nombre del negocio"); return }
        if (deliveryTime.isEmpty()) { showError("Escribe el tiempo de entrega estimado"); return }

        // Duplicate name check (skip if editing the same business)
        val ownerEmail = UserPrefsManager.getEmail()
        val existing = BusinessRepository.getByOwner(ownerEmail)
        val duplicate = existing.any { it.name.equals(name, ignoreCase = true) && it.id != editBusinessId }
        if (duplicate) { showError("Ya tienes un negocio registrado con ese nombre"); return }

        val deliveryFee = deliveryFeeStr.toIntOrNull() ?: 0
        val minimum = minimumStr.toIntOrNull() ?: 0

        val menuItems = mutableListOf<MenuItem>()
        for (i in 0 until binding.layoutMenuItems.childCount) {
            val child = binding.layoutMenuItems.getChildAt(i)
            @Suppress("UNCHECKED_CAST")
            val triple = child.tag as? Triple<EditText, EditText, EditText> ?: continue
            val (emojiEdit, nameEdit, priceEdit) = triple
            val itemName = nameEdit.text?.toString()?.trim() ?: ""
            if (itemName.isEmpty()) continue
            val itemPrice = priceEdit.text?.toString()?.trim()?.toIntOrNull() ?: 0
            val itemEmoji = emojiEdit.text?.toString()?.trim().let { if (it.isNullOrEmpty()) "🍽️" else it }
            menuItems.add(MenuItem(
                id = kotlin.math.abs((name + itemName + i).hashCode()),
                name = itemName,
                description = "",
                price = itemPrice,
                emoji = itemEmoji,
                isPopular = false
            ))
        }

        val business = BusinessData(
            id = editBusinessId ?: BusinessRepository.newId(),
            ownerEmail = ownerEmail,
            name = name,
            category = selectedCategory,
            emoji = emoji.ifEmpty { "🍽️" },
            tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            deliveryTime = deliveryTime,
            deliveryFee = deliveryFee,
            minimumOrder = minimum,
            phone = phone,
            promo = promo.ifEmpty { null },
            isOpen = isOpen,
            imageUri = selectedImageUri?.toString() ?: "",
            menuItems = menuItems
        )

        binding.btnRegisterBusiness.isEnabled = false
        binding.btnRegisterBusiness.text = if (editBusinessId != null) "Guardando..." else "Registrando..."

        binding.btnRegisterBusiness.animate()
            .scaleX(1.04f).scaleY(1.04f).setDuration(120).withEndAction {
                binding.btnRegisterBusiness.animate().scaleX(1f).scaleY(1f).setDuration(100).withEndAction {
                    BusinessRepository.save(business)
                    binding.btnRegisterBusiness.text = "✓  Guardado"
                    binding.textError.visibility = View.GONE
                    binding.root.postDelayed({
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }, 800)
                }.start()
            }.start()
    }

    private fun showError(msg: String) {
        binding.textError.text = msg
        binding.textError.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(binding.textError, "translationX", 0f, -12f, 12f, -8f, 8f, -4f, 4f, 0f)
            .apply { duration = 450; start() }
    }

    private fun animateEntrance() {
        val views = listOf<View>(
            binding.editBizName, binding.editBizEmoji,
            binding.editBizTags, binding.editBizPromo,
            binding.layoutImagePicker,
            binding.editBizDeliveryTime, binding.editBizDeliveryFee,
            binding.editBizMinimum, binding.editBizPhone,
            binding.btnRegisterBusiness
        )
        views.forEachIndexed { i, view ->
            view.alpha = 0f
            view.translationY = 32f
            view.animate().alpha(1f).translationY(0f)
                .setStartDelay(60L + i * 40L).setDuration(360)
                .setInterpolator(DecelerateInterpolator(2f)).start()
        }
    }
}
