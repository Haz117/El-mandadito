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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.elmandadito.app.R
import com.elmandadito.app.data.CartRepository
import com.elmandadito.app.data.FavoritesManager
import com.elmandadito.app.data.Restaurant
import com.elmandadito.app.data.SampleData
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
@Composable
fun HomeScreen(
    onRestaurantClick: (Restaurant) -> Unit = {},
    onCartClick: () -> Unit = {}
) {
    val cartItems  by CartRepository.items.observeAsState(mutableListOf())
    val cartCount  = cartItems.sumOf { it.quantity }
    var selectedCat by remember { mutableStateOf("all") }
    val restaurants = remember(selectedCat) {
        if (selectedCat == "all") SampleData.restaurants
        else SampleData.restaurants.filter { it.category == selectedCat }
    }
    val openCount = remember(selectedCat) { restaurants.count { it.isOpen } }

    // ── Screen fade-in entrance ────────────────────────────────────────────
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); loaded = true }
    val screenAlpha by animateFloatAsState(if (loaded) 1f else 0f, tween(500), label = "sa")

    Box(Modifier.fillMaxSize().background(AppWhite).alpha(screenAlpha)) {
        LazyColumn(contentPadding = PaddingValues(bottom = if (cartCount > 0) 96.dp else 24.dp)) {

            item { TopHeader(openCount) }
            item { SearchRow(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) }
            item { CategoryRow(selectedCat, Modifier.padding(top = 2.dp, bottom = 4.dp)) { selectedCat = it } }

            item(key = "divider1") { SectionDivider() }

            item(key = "promoHeader") {
                SectionHeader(
                    "Promociones",
                    Modifier
                        .animateItem(fadeInSpec = tween(350))
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 12.dp)
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
                            fontWeight = FontWeight.Medium, modifier = Modifier.clickable {}
                        )
                    }
                }
            }

            items(restaurants, key = { it.id }) { r ->
                RestaurantCard(
                    restaurant = r,
                    onClick    = { onRestaurantClick(r) },
                    modifier   = Modifier
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                        .animateItem(fadeInSpec = tween(300))
                )
            }
            if (restaurants.isEmpty()) item { EmptyState() }
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

// ─── SECTION DIVIDER ─────────────────────────────────────────────────────────
@Composable
private fun SectionDivider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(8.dp).background(LightGray))
}

// ─── TOP HEADER ──────────────────────────────────────────────────────────────
@Composable
private fun TopHeader(openCount: Int) {
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
            Column {
                Text("Entregar en", color = Color(0xFF757575), fontSize = 11.sp, letterSpacing = 0.3.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, null, tint = AppWhite, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(
                        "Calle Reforma 456", color = AppWhite, fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 220.dp)
                    )
                    Icon(Icons.Filled.KeyboardArrowDown, null, tint = Color(0xFF757575), modifier = Modifier.size(18.dp))
                }
            }
            Box(
                Modifier.size(40.dp).background(Color(0xFF2A2A2A), RoundedCornerShape(12.dp)).clickable {},
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
            "¿Qué se te antoja hoy?",
            color = AppWhite, fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp, lineHeight = 32.sp
        )

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
fun SearchRow(modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }

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
                    onValueChange = { query = it },
                    textStyle = TextStyle(color = NearBlack, fontSize = 14.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                IconButton({ query = "" }, Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, "Limpiar", tint = MidGray, modifier = Modifier.size(15.dp))
                }
            }
        }
        Box(
            Modifier.size(50.dp).background(NearBlack, RoundedCornerShape(25.dp)).clickable {},
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Tune, "Filtros", tint = AppWhite, modifier = Modifier.size(22.dp))
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
                // Food emoji — gentle float
                Text(
                    restaurant.emoji, fontSize = 68.sp,
                    modifier = Modifier.align(Alignment.Center).offset(y = emojiY.dp)
                )

                // Closed overlay — dims card when restaurant is not open
                if (!restaurant.isOpen) {
                    Box(Modifier.fillMaxSize().background(AppBlack.copy(alpha = 0.52f)))
                }

                // Promo badge (top-start)
                if (restaurant.promo != null) {
                    Box(
                        Modifier.padding(12.dp).align(Alignment.TopStart)
                            .background(AppWhite, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            restaurant.promo, color = NearBlack, fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = 0.3.sp
                        )
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
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
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
            fontWeight = FontWeight.Medium, modifier = Modifier.clickable {}
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() { MaterialTheme { HomeScreen() } }
