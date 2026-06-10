package com.elmandadito.app.compose

import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.elmandadito.app.R
import com.elmandadito.app.data.AddressManager
import com.elmandadito.app.data.BusinessRepository
import com.elmandadito.app.data.CartRepository
import com.elmandadito.app.data.FavoritesManager
import com.elmandadito.app.data.OrderHistoryManager
import com.elmandadito.app.data.Restaurant
import com.elmandadito.app.data.SampleData
import com.elmandadito.app.data.SearchHistoryManager
import com.elmandadito.app.data.UserPrefsManager
import kotlinx.coroutines.delay
import java.util.Calendar

// ─── Palette ─────────────────────────────────────────────────────────────────
private val AppBlack  = Color(0xFF000000)
private val NearBlack = Color(0xFF1A1A1A)
private val DarkGray  = Color(0xFF3D3D3D)
private val MidGray   = Color(0xFF6B6B6B)
private val LightGray = Color(0xFFF6F6F6)
private val Border    = Color(0xFFEBEBEB)
private val AppWhite  = Color(0xFFFFFFFF)
private val BrandBlack = Color(0xFF000000)
private val OpenGreen = Color(0xFF06C167)
private val ImgBg     = Color(0xFF141414)

// ─── Helpers ─────────────────────────────────────────────────────────────────
private fun greeting(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when { h < 12 -> "Buenos días"; h < 19 -> "Buenas tardes"; else -> "Buenas noches" }
}

// ─── Static data ─────────────────────────────────────────────────────────────
private data class CategoryDef(val id: String, val label: String, @DrawableRes val icon: Int)
private val allCategories = listOf(
    CategoryDef("all",      "Todos",   R.drawable.ic_home),
    CategoryDef("mexican",  "Tacos",   R.drawable.ic_food_mexican),
    CategoryDef("burgers",  "Burgers", R.drawable.ic_food_burger),
    CategoryDef("pizza",    "Pizza",   R.drawable.ic_food_pizza),
    CategoryDef("sushi",    "Sushi",   R.drawable.ic_food_sushi),
    CategoryDef("chicken",  "Pollo",   R.drawable.ic_food_chicken),
    CategoryDef("desserts", "Postres", R.drawable.ic_food_dessert),
)

private data class PromoCard(val title: String, val sub: String, val tag: String, val emoji: String)
private val samplePromos = listOf(
    PromoCard("20% en tu\nprimer pedido",  "Código: BIENVENIDO",       "NUEVO",    "🎉"),
    PromoCard("Envío GRATIS\nesta semana", "Pedidos mayores a \$150",  "OFERTA",   "🛵"),
    PromoCard("2×1 en combos\ndel día",    "Solo hoy hasta las 10 pm", "LIMITADO", "⚡"),
)

// ─── SCREEN ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    networkRestaurants: List<Restaurant> = emptyList(),
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onRestaurantClick: (Restaurant) -> Unit = {},
    onCartClick: () -> Unit = {}
) {
    val ctx = LocalContext.current
    SearchHistoryManager.init(ctx)
    OrderHistoryManager.init(ctx)
    UserPrefsManager.init(ctx)
    val cartItems  by CartRepository.items.observeAsState(mutableListOf())
    val cartCount  = cartItems.sumOf { it.quantity }
    val userName   = remember { UserPrefsManager.getName().let { if (it == "Usuario") "" else it } }
    val lastOrder  = remember { OrderHistoryManager.getOrders().firstOrNull() }
    var selectedCat by remember { mutableStateOf("all") }
    var query by remember { mutableStateOf("") }
    var searchFocused by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("default") }
    var openOnly by remember { mutableStateOf(false) }
    var currentAddress by remember { mutableStateOf(AddressManager.getSelectedLabel()) }
    var showAddressDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showPromosDialog by remember { mutableStateOf(false) }
    val filtersActive = sortBy != "default" || openOnly

    // Refresh restaurant list when returning from other screens (e.g. after adding a business)
    var refreshKey by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshKey++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val restaurants = remember(selectedCat, query, sortBy, openOnly, refreshKey, networkRestaurants) {
        val localRestaurants = if (networkRestaurants.isNotEmpty()) networkRestaurants
                               else SampleData.restaurants
        val allRestaurants = localRestaurants + BusinessRepository.getAll().map { it.toRestaurant() }
        var base = if (selectedCat == "all") allRestaurants
                   else allRestaurants.filter { it.category == selectedCat }
        if (query.isNotBlank()) base = base.filter { r ->
            r.name.contains(query, ignoreCase = true) ||
            r.tags.any { t -> t.contains(query, ignoreCase = true) } ||
            r.menu.flatMap { it.items }.any { item ->
                item.name.contains(query, ignoreCase = true) ||
                item.description.contains(query, ignoreCase = true)
            }
        }
        if (openOnly) base = base.filter { it.isOpen }
        when (sortBy) {
            "rating" -> base.sortedByDescending { it.rating }
            "time"   -> base.sortedBy { it.deliveryTime.filter { c -> c.isDigit() }.toIntOrNull() ?: 99 }
            "fee"    -> base.sortedBy { it.deliveryFee }
            else     -> base
        }
    }
    val openCount = restaurants.count { it.isOpen }

    // ── Loading / skeleton ────────────────────────────────────────────────
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(550); loaded = true }
    val contentAlpha by animateFloatAsState(if (loaded) 1f else 0f, tween(400), label = "ca")
    val skeletonAlpha by animateFloatAsState(if (loaded) 0f else 1f, tween(280), label = "ska")

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
    Box(Modifier.fillMaxSize().background(AppWhite)) {
    Box(Modifier.fillMaxSize().alpha(contentAlpha)) {
        LazyColumn(contentPadding = PaddingValues(bottom = if (cartCount > 0) 96.dp else 24.dp)) {

            item { TopHeader(openCount, currentAddress, userName, { showAddressDialog = true }, { Toast.makeText(ctx, "Sin notificaciones nuevas", Toast.LENGTH_SHORT).show() }) }
            item {
                SearchRow(
                    query = query,
                    onQueryChange = { query = it; if (it.isBlank()) searchFocused = true },
                    onFocusChange = { searchFocused = it },
                    onFilterClick = { showFilterDialog = true },
                    filtersActive = filtersActive,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }
            if (lastOrder != null && query.isEmpty() && !searchFocused) {
                item(key = "reorderBanner") {
                    ReorderBanner(
                        restaurantName = lastOrder.restaurantName,
                        total = lastOrder.total,
                        onClick = {
                            val all = networkRestaurants.ifEmpty { SampleData.restaurants } +
                                BusinessRepository.getAll().map { it.toRestaurant() }
                            val restaurant = all.find { it.name == lastOrder.restaurantName }
                            if (restaurant != null) onRestaurantClick(restaurant)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
            if (searchFocused && query.isEmpty()) {
                item(key = "searchHistory") {
                    SearchHistoryDropdown(
                        history = SearchHistoryManager.getAll(),
                        onSelect = { q -> query = q; searchFocused = false },
                        onRemove = { SearchHistoryManager.remove(it) },
                        onClear  = { SearchHistoryManager.clear() },
                        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
                    )
                }
            }
            item { CategoryRow(selectedCat, Modifier.padding(top = 2.dp, bottom = 4.dp)) { selectedCat = it } }

            item(key = "divider1") { SectionDivider() }

            item(key = "promoHeader") {
                SectionHeader(
                    "Promociones",
                    Modifier
                        .animateItem(fadeInSpec = tween(350))
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 12.dp),
                    onVerTodo = { showPromosDialog = true }
                )
            }
            item(key = "promoBanners") {
                PromoBanners(Modifier.animateItem(fadeInSpec = tween(400)))
            }

            item(key = "divider2") { SectionDivider(Modifier.padding(top = 20.dp)) }

            item(key = "nearbyHeader") {
                Row(
                    Modifier.padding(horizontal = 20.dp).padding(top = 20.dp, bottom = 12.dp),
                    Arrangement.SpaceBetween, Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.width(4.dp).height(20.dp)
                                .background(BrandBlack, RoundedCornerShape(2.dp))
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Restaurantes cercanos", color = NearBlack, fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (restaurants.isNotEmpty()) {
                            Text("${restaurants.size}", color = MidGray, fontSize = 12.sp)
                            Spacer(Modifier.width(3.dp))
                        }
                        Text(
                            "Ver todo", color = MidGray, fontSize = 13.sp,
                            fontWeight = FontWeight.Medium, modifier = Modifier.clickable {
                                selectedCat = "all"; sortBy = "default"; openOnly = false; query = ""
                            }
                        )
                    }
                }
            }

            items(restaurants, key = { it.id }) { r ->
                RestaurantCard(
                    restaurant = r,
                    onClick    = {
                        if (query.isNotBlank()) SearchHistoryManager.add(query)
                        searchFocused = false
                        onRestaurantClick(r)
                    },
                    modifier   = Modifier
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                        .animateItem(fadeInSpec = tween(300))
                )
            }
            if (restaurants.isEmpty()) item {
                if (query.isNotBlank()) SearchEmptyState(query) else EmptyState()
            }
        }

        AnimatedVisibility(
            visible  = cartCount > 0,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter    = slideInVertically { it } + fadeIn(tween(280)),
            exit     = slideOutVertically { it } + fadeOut(tween(200))
        ) {
            CartBar(cartCount, cartItems.sumOf { it.totalPrice }, onCartClick)
        }
    }
    }
    if (skeletonAlpha > 0.01f) {
        Box(Modifier.fillMaxSize().background(AppWhite).alpha(skeletonAlpha)) {
            Column {
                Box(Modifier.fillMaxWidth().height(180.dp).background(AppBlack))
                repeat(4) { SkeletonRestaurantCard() }
            }
        }
    }
    } // PullToRefreshBox

    if (showAddressDialog) {
        AddressDialog(
            currentSelected = AddressManager.getSelected(),
            onDismiss = { showAddressDialog = false },
            onSelect = { idx ->
                AddressManager.setSelected(idx)
                currentAddress = AddressManager.getSelectedLabel()
                showAddressDialog = false
            }
        )
    }
    if (showFilterDialog) {
        FilterDialog(
            sortBy = sortBy,
            openOnly = openOnly,
            onSortChange = { sortBy = it },
            onOpenOnlyChange = { openOnly = it },
            onDismiss = { showFilterDialog = false }
        )
    }
    if (showPromosDialog) {
        PromosDialog(onDismiss = { showPromosDialog = false })
    }
}

// ─── SKELETON LOADING ────────────────────────────────────────────────────────
@Composable
private fun SkeletonBox(modifier: Modifier = Modifier) {
    val shimmer = rememberInfiniteTransition(label = "sk")
    val x by shimmer.animateFloat(
        initialValue = -600f, targetValue = 600f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "skx"
    )
    Box(modifier.background(
        Brush.linearGradient(
            listOf(Color(0xFFE8E8E8), Color(0xFFF5F5F5), Color(0xFFE8E8E8)),
            start = Offset(x, 0f), end = Offset(x + 400f, 300f)
        )
    ))
}

@Composable
private fun SkeletonRestaurantCard() {
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 7.dp)
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .background(AppWhite, RoundedCornerShape(20.dp))
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            SkeletonBox(Modifier.size(90.dp).clip(RoundedCornerShape(16.dp)))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBox(Modifier.fillMaxWidth(0.65f).height(14.dp).clip(RoundedCornerShape(7.dp)))
                SkeletonBox(Modifier.fillMaxWidth(0.85f).height(10.dp).clip(RoundedCornerShape(5.dp)))
                SkeletonBox(Modifier.fillMaxWidth(0.45f).height(10.dp).clip(RoundedCornerShape(5.dp)))
            }
        }
    }
}

// ─── SECTION DIVIDER ─────────────────────────────────────────────────────────
@Composable
private fun SectionDivider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(8.dp).background(LightGray))
}

// ─── TOP HEADER ──────────────────────────────────────────────────────────────
@Composable
private fun TopHeader(openCount: Int, address: String, userName: String, onAddressClick: () -> Unit, onBellClick: () -> Unit) {
    // Bell shake animation
    val bellAnim = rememberInfiniteTransition(label = "bell")
    val bellRot by bellAnim.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 4000
                0f at 0; -8f at 200; 8f at 400; -5f at 600; 5f at 800; 0f at 1000; 0f at 4000
            },
            RepeatMode.Restart
        ), label = "br"
    )

    // Green dot pulse
    val pulse = rememberInfiniteTransition(label = "dot")
    val dotSc by pulse.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ds"
    )

    Column(Modifier.fillMaxWidth().background(AppBlack).padding(horizontal = 20.dp, vertical = 20.dp)) {

        // Address row
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.clickable { onAddressClick() }) {
                Text("Entregar en", color = Color(0xFF757575), fontSize = 11.sp, letterSpacing = 0.3.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, null, tint = AppWhite, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(
                        address, color = AppWhite, fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 220.dp)
                    )
                    Icon(Icons.Filled.KeyboardArrowDown, null, tint = Color(0xFF757575), modifier = Modifier.size(18.dp))
                }
            }
            Box(
                Modifier.size(40.dp).background(Color(0xFF2A2A2A), RoundedCornerShape(12.dp)).clickable { onBellClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Notifications, "Notificaciones", tint = AppWhite,
                    modifier = Modifier.size(22.dp).graphicsLayer { rotationZ = bellRot }
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        Text(greeting(), color = Color(0xFF757575), fontSize = 13.sp, letterSpacing = 0.2.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            if (userName.isNotBlank()) "¡Hola, $userName!" else "¿Qué se te antoja hoy?",
            color = AppWhite, fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp, lineHeight = 32.sp
        )
        if (userName.isNotBlank()) {
            Spacer(Modifier.height(3.dp))
            Text("¿Qué se te antoja hoy?", color = Color(0xFF9E9E9E), fontSize = 14.sp)
        }

        if (openCount > 0) {
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(6.dp).scale(dotSc).background(OpenGreen, CircleShape))
                Spacer(Modifier.width(7.dp))
                Text(
                    "$openCount restaurantes abiertos", color = Color(0xFFBDBDBD),
                    fontSize = 12.sp, fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

// ─── SEARCH ROW ──────────────────────────────────────────────────────────────
@Composable
fun SearchRow(
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    onFocusChange: (Boolean) -> Unit = {},
    onFilterClick: () -> Unit = {},
    filtersActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(modifier, Arrangement.spacedBy(10.dp), Alignment.CenterVertically) {
        Row(
            Modifier.weight(1f).height(50.dp)
                .background(LightGray, RoundedCornerShape(25.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Search, null, tint = MidGray, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) Text("Busca restaurantes o platillos", color = MidGray, fontSize = 14.sp)
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = TextStyle(color = NearBlack, fontSize = 14.sp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { onFocusChange(it.isFocused) }
                )
            }
            if (query.isNotEmpty()) {
                IconButton({ onQueryChange("") }, Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, "Limpiar", tint = MidGray, modifier = Modifier.size(15.dp))
                }
            }
        }
        Box(
            Modifier.size(50.dp).background(NearBlack, RoundedCornerShape(25.dp)).clickable { onFilterClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Tune, "Filtros", tint = AppWhite, modifier = Modifier.size(22.dp))
            if (filtersActive) {
                Box(
                    Modifier.size(10.dp).background(Color(0xFF06C167), CircleShape)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}

// ─── CATEGORIES ──────────────────────────────────────────────────────────────
@Composable
fun CategoryRow(selected: String, modifier: Modifier = Modifier, onSelect: (String) -> Unit) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(allCategories) { cat ->
            val isSel = cat.id == selected
            val sc by animateFloatAsState(
                if (isSel) 1.06f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "cs"
            )
            val dotW by animateDpAsState(if (isSel) 18.dp else 4.dp, tween(220), label = "dw")
            val dotC by animateColorAsState(if (isSel) NearBlack else Color.Transparent, tween(180), label = "dc")

            // Shimmer overlay for selected category
            val shimmer = rememberInfiniteTransition(label = "shimmer")
            val shimAlpha by shimmer.animateFloat(
                initialValue = 0f, targetValue = 0.12f,
                animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
                label = "sha"
            )

            Column(
                Modifier.scale(sc).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onSelect(cat.id) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.size(62.dp)
                        .shadow(if (isSel) 4.dp else 0.dp, CircleShape)
                        .background(if (isSel) NearBlack else LightGray, CircleShape)
                        .border(1.dp, if (isSel) NearBlack else Border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(cat.icon), null,
                        tint = if (isSel) AppWhite else NearBlack,
                        modifier = Modifier.size(26.dp)
                    )
                    // Shimmer overlay when selected
                    if (isSel) Box(Modifier.fillMaxSize().background(AppWhite.copy(shimAlpha), CircleShape))
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    cat.label,
                    color = if (isSel) NearBlack else MidGray,
                    fontSize = 11.sp,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                // Animated pill indicator
                Box(Modifier.height(3.dp).width(dotW).background(dotC, CircleShape))
            }
        }
    }
}

// ─── PROMO BANNERS ───────────────────────────────────────────────────────────
@Composable
fun PromoBanners(modifier: Modifier = Modifier) {
    var current by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(3600); current = (current + 1) % samplePromos.size } }

    Column(modifier) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(samplePromos) { i, p ->
                val isActive = i == current
                val sc by animateFloatAsState(if (isActive) 1f else 0.96f, tween(300), label = "ps")
                val elevation by animateDpAsState(if (isActive) 8.dp else 1.dp, label = "pe")

                // Emoji sway when card is active
                val swayAnim = rememberInfiniteTransition(label = "sway")
                val rotation by swayAnim.animateFloat(
                    initialValue = -10f, targetValue = 10f,
                    animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "sw"
                )

                Box(
                    Modifier.width(288.dp).height(118.dp).scale(sc)
                        .shadow(elevation, RoundedCornerShape(20.dp))
                        .background(NearBlack, RoundedCornerShape(20.dp))
                        .clickable {}
                ) {
                    // Left accent stripe
                    Box(
                        Modifier.width(3.dp).fillMaxHeight()
                            .background(
                                Brush.verticalGradient(listOf(AppWhite.copy(0.6f), AppWhite.copy(0.1f))),
                                RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                            )
                    )
                    Row(
                        Modifier.fillMaxSize().padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                        Arrangement.SpaceBetween, Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Box(
                                Modifier.background(AppWhite.copy(0.15f), RoundedCornerShape(6.dp))
                                    .border(0.5.dp, AppWhite.copy(0.3f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    p.tag, color = AppWhite, fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp
                                )
                            }
                            Spacer(Modifier.height(9.dp))
                            Text(
                                p.title, color = AppWhite, fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold, lineHeight = 21.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(p.sub, color = Color(0xFF9E9E9E), fontSize = 11.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            p.emoji, fontSize = 44.sp,
                            modifier = Modifier.graphicsLayer { rotationZ = if (isActive) rotation else 0f }
                        )
                    }
                }
            }
        }
        // Indicator dots
        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp),
            Arrangement.Center, Alignment.CenterVertically
        ) {
            samplePromos.forEachIndexed { i, _ ->
                val w by animateDpAsState(if (i == current) 22.dp else 5.dp, tween(220), label = "d$i")
                val c by animateColorAsState(if (i == current) NearBlack else Border, tween(200), label = "dc$i")
                Box(Modifier.padding(horizontal = 3.dp).height(5.dp).width(w).background(c, CircleShape))
            }
        }
    }
}

// ─── RESTAURANT CARD ─────────────────────────────────────────────────────────
@Composable
fun RestaurantCard(restaurant: Restaurant, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val initFav = remember { runCatching { FavoritesManager.isFavorite(restaurant.id) }.getOrDefault(false) }
    var isFav   by remember { mutableStateOf(initFav) }
    var pressed by remember { mutableStateOf(false) }

    val sc by animateFloatAsState(
        if (pressed) 0.98f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "rc"
    )

    // Emoji float animation
    val floatAnim = rememberInfiniteTransition(label = "float")
    val emojiY by floatAnim.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ey"
    )

    Card(
        modifier  = modifier.fillMaxWidth().scale(sc).pointerInput(Unit) {
            detectTapGestures(
                onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                onTap   = { onClick() }
            )
        },
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
        border    = BorderStroke(1.dp, Border),
        colors    = CardDefaults.cardColors(containerColor = AppWhite)
    ) {
        Column {
            // ── Image area ─────────────────────────────────────────────────
            Box(
                Modifier.fillMaxWidth().height(168.dp)
                    .background(Brush.verticalGradient(listOf(Color(0xFF222222), ImgBg, Color(0xFF0A0A0A))))
            ) {
                if (restaurant.imageUri.isNotBlank()) {
                    AsyncImage(
                        model = restaurant.imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        restaurant.emoji, fontSize = 68.sp,
                        modifier = Modifier.align(Alignment.Center).offset(y = emojiY.dp)
                    )
                }

                // Closed overlay — dims card when restaurant is not open
                if (!restaurant.isOpen) {
                    Box(Modifier.fillMaxSize().background(AppBlack.copy(alpha = 0.52f)))
                }

                // Promo + New badges (top-start)
                if (restaurant.promo != null || restaurant.isNew) {
                    Column(
                        Modifier.padding(12.dp).align(Alignment.TopStart),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (restaurant.promo != null) {
                            Box(
                                Modifier.background(AppWhite, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    restaurant.promo, color = NearBlack, fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold, letterSpacing = 0.3.sp
                                )
                            }
                        }
                        if (restaurant.isNew) {
                            Box(
                                Modifier.background(OpenGreen, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    "NUEVO", color = AppWhite, fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                // Rating pill (top-end)
                Row(
                    Modifier.padding(12.dp).align(Alignment.TopEnd)
                        .background(AppWhite, RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Star, null, tint = Color(0xFFFF9500), modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("${restaurant.rating}", color = NearBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Open/closed badge (bottom-start)
                Row(
                    Modifier.padding(12.dp).align(Alignment.BottomStart)
                        .background(
                            if (restaurant.isOpen) OpenGreen else DarkGray,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(5.dp).background(AppWhite.copy(0.9f), CircleShape))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        if (restaurant.isOpen) "Abierto" else "Cerrado",
                        color = AppWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold
                    )
                }

                // Fav button (bottom-end)
                FavoriteButton(
                    isFav,
                    { isFav = !isFav; runCatching { FavoritesManager.toggleFavorite(restaurant.id) } },
                    Modifier.align(Alignment.BottomEnd).padding(10.dp)
                )
            }

            // ── Info ───────────────────────────────────────────────────────
            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

                // Name + chevron
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(
                        restaurant.name, color = NearBlack, fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFFCCCCCC), modifier = Modifier.size(18.dp))
                }

                // Tag chips
                Row(Modifier.padding(top = 7.dp, bottom = 10.dp)) {
                    restaurant.tags.take(3).forEach { tag ->
                        Box(
                            Modifier.padding(end = 6.dp)
                                .background(LightGray, RoundedCornerShape(20.dp))
                                .border(1.dp, Border, RoundedCornerShape(20.dp))
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(tag, color = MidGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Meta row
                HorizontalDivider(color = Border, thickness = 0.8.dp, modifier = Modifier.padding(bottom = 10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, tint = MidGray, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(restaurant.deliveryTime, color = MidGray, fontSize = 12.sp)
                    MetaDot()
                    Icon(
                        Icons.Filled.DeliveryDining, null,
                        tint = if (restaurant.deliveryFee == 0) OpenGreen else MidGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (restaurant.deliveryFee == 0) "Envío gratis" else "\$${restaurant.deliveryFee}",
                        color = if (restaurant.deliveryFee == 0) OpenGreen else MidGray,
                        fontSize = 12.sp,
                        fontWeight = if (restaurant.deliveryFee == 0) FontWeight.SemiBold else FontWeight.Normal
                    )
                    MetaDot()
                    Text("Mín. \$${restaurant.minimumOrder}", color = MidGray, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun MetaDot() {
    Box(Modifier.padding(horizontal = 6.dp).size(3.dp).background(Border, CircleShape))
}

// ─── FAVORITE BUTTON ─────────────────────────────────────────────────────────
@Composable
fun FavoriteButton(isFav: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val sc by animateFloatAsState(
        if (isFav) 1.25f else 1f,
        spring(Spring.DampingRatioLowBouncy, Spring.StiffnessHigh), label = "fav"
    )
    Box(
        modifier.size(38.dp).background(AppWhite.copy(0.95f), CircleShape).clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            "Favorito",
            tint = if (isFav) BrandBlack else NearBlack.copy(0.65f),
            modifier = Modifier.size(18.dp).scale(sc)
        )
    }
}

// ─── SECTION HEADER ──────────────────────────────────────────────────────────
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, onVerTodo: () -> Unit = {}) {
    Row(modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(20.dp).background(BrandBlack, RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(10.dp))
            Text(
                title, color = NearBlack, fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp
            )
        }
        Text(
            "Ver todo", color = MidGray, fontSize = 13.sp,
            fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onVerTodo() }
        )
    }
}

// ─── CART BAR ────────────────────────────────────────────────────────────────
@Composable
fun CartBar(count: Int, total: Int, onClick: () -> Unit) {
    // Badge bounce when count increases
    var prevCount by remember { mutableIntStateOf(count) }
    var badgeBounce by remember { mutableStateOf(false) }
    val badgeSc by animateFloatAsState(
        if (badgeBounce) 1.4f else 1f,
        spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium),
        label = "bc"
    )
    LaunchedEffect(count) {
        if (count > prevCount) { badgeBounce = true; delay(200); badgeBounce = false }
        prevCount = count
    }

    Box(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
            .shadow(20.dp, RoundedCornerShape(20.dp))
            .background(NearBlack, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(30.dp).scale(badgeSc).background(AppWhite, RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) { Text("$count", color = NearBlack, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold) }
                Spacer(Modifier.width(12.dp))
                Text("Ver mi carrito", color = AppWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("\$$total", color = AppWhite, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier.size(22.dp).background(Color(0xFF2A2A2A), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFF9E9E9E), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ─── SEARCH EMPTY STATE ───────────────────────────────────────────────────────
@Composable
private fun SearchEmptyState(query: String) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 52.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(80.dp).background(LightGray, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) { Text("🔍", fontSize = 36.sp) }
        Spacer(Modifier.height(16.dp))
        Text("Sin resultados para", color = MidGray, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "\"$query\"", color = NearBlack, fontSize = 18.sp,
            fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp
        )
        Spacer(Modifier.height(8.dp))
        Text("Intenta con otro nombre o categoría", color = MidGray, fontSize = 13.sp)
    }
}

// ─── EMPTY STATE ─────────────────────────────────────────────────────────────
@Composable
private fun EmptyState() {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 52.dp),
        Arrangement.Center, Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(80.dp).background(LightGray, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) { Text("🍽️", fontSize = 36.sp) }
        Spacer(Modifier.height(16.dp))
        Text("Sin resultados", color = NearBlack, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("Intenta con otra categoría o búsqueda", color = MidGray, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

// ─── ADDRESS DIALOG ──────────────────────────────────────────────────────────
@Composable
private fun AddressDialog(currentSelected: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val options = listOf(
        "Casa" to AddressManager.fixedAddresses[0].second,
        "Trabajo" to AddressManager.fixedAddresses[1].second,
        "Personalizada" to AddressManager.getCustomAddress()
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecciona dirección", fontWeight = FontWeight.Bold, color = NearBlack) },
        text = {
            Column {
                options.forEachIndexed { idx, (label, sub) ->
                    Row(
                        Modifier.fillMaxWidth()
                            .selectable(selected = idx == currentSelected, onClick = { onSelect(idx) })
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = idx == currentSelected, onClick = { onSelect(idx) })
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(label, fontWeight = FontWeight.SemiBold, color = NearBlack, fontSize = 15.sp)
                            Text(sub, color = MidGray, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = NearBlack) } }
    )
}

// ─── FILTER DIALOG ───────────────────────────────────────────────────────────
@Composable
private fun FilterDialog(
    sortBy: String, openOnly: Boolean,
    onSortChange: (String) -> Unit, onOpenOnlyChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sorts = listOf(
        "default" to "Por defecto",
        "rating"  to "Calificación",
        "time"    to "Tiempo de entrega",
        "fee"     to "Costo de envío"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtros y orden", fontWeight = FontWeight.Bold, color = NearBlack) },
        text = {
            Column {
                Text("Ordenar por", color = MidGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp))
                sorts.forEach { (id, label) ->
                    Row(
                        Modifier.fillMaxWidth()
                            .selectable(selected = sortBy == id, onClick = { onSortChange(id) })
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = sortBy == id, onClick = { onSortChange(id) })
                        Spacer(Modifier.width(10.dp))
                        Text(label, color = NearBlack, fontSize = 14.sp)
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Border)
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Solo restaurantes abiertos", color = NearBlack, fontSize = 14.sp)
                    Switch(checked = openOnly, onCheckedChange = onOpenOnlyChange)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Listo", fontWeight = FontWeight.Bold, color = NearBlack)
            }
        }
    )
}

// ─── PROMOS DIALOG ───────────────────────────────────────────────────────────
@Composable
private fun PromosDialog(onDismiss: () -> Unit) {
    val promos = listOf(
        Triple("MANDADITO20", "-20% en tu pedido", "Aplica en todos los restaurantes"),
        Triple("BIENVENIDO",  "-15% bienvenida",   "Solo para usuarios nuevos"),
        Triple("PROMO10",     "-10% siempre",      "Válido en cualquier pedido")
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Códigos de descuento", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                promos.forEach { (code, discount, desc) ->
                    Row(
                        Modifier.fillMaxWidth()
                            .background(LightGray, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        Arrangement.SpaceBetween,
                        Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(code, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp,
                                color = NearBlack, fontFamily = FontFamily.Monospace)
                            Text(desc, fontSize = 11.sp, color = MidGray,
                                modifier = Modifier.padding(top = 2.dp))
                        }
                        Text(discount, color = OpenGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                Text(
                    "Aplica el código desde el carrito antes de confirmar tu pedido.",
                    fontSize = 11.sp, color = MidGray
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido", fontWeight = FontWeight.Bold, color = NearBlack)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = AppWhite
    )
}

// ─── SEARCH HISTORY DROPDOWN ─────────────────────────────────────────────────
@Composable
private fun SearchHistoryDropdown(
    history: List<String>,
    onSelect: (String) -> Unit,
    onRemove: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) return
    Column(
        modifier
            .fillMaxWidth()
            .background(AppWhite, RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(vertical = 6.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            Arrangement.SpaceBetween, Alignment.CenterVertically
        ) {
            Text("Búsquedas recientes", color = MidGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Borrar todo", color = NearBlack, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onClear() }
            )
        }
        history.forEach { q ->
            Row(
                Modifier.fillMaxWidth()
                    .clickable { onSelect(q) }
                    .padding(horizontal = 16.dp, vertical = 11.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.History, null, tint = MidGray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(q, color = NearBlack, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(
                    Icons.Filled.Close, "Eliminar",
                    tint = MidGray,
                    modifier = Modifier.size(16.dp).clickable { onRemove(q) }
                )
            }
        }
    }
}

// ─── REORDER BANNER ──────────────────────────────────────────────────────────
@Composable
private fun ReorderBanner(
    restaurantName: String,
    total: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(LightGray, RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                Modifier.size(36.dp).background(NearBlack, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) { Text("🔄", fontSize = 16.sp) }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Pedir de nuevo", color = NearBlack, fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    restaurantName, color = MidGray, fontSize = 12.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("\$$total", color = MidGray, fontSize = 12.sp)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Filled.ChevronRight, null, tint = MidGray, modifier = Modifier.size(16.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() { MaterialTheme { HomeScreen() } }
