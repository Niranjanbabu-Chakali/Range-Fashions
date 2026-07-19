package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.Product
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel,
    onShowSnackbar: (String) -> Unit
) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val wishlistItems by viewModel.wishlistItems.collectAsStateWithLifecycle()

    var isScreenLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1000)
        isScreenLoading = false
    }

    if (isScreenLoading) {
        HomeScreenSkeleton()
    } else {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
        ) {
            // 1. Announcement Bar
            AnnouncementBar()

            // 2. Main Hero Banner Slider
            HeroBannerSlider(navController)

            // 3. Shop By Category
            CategoryCardsSection(navController)

            // 4. Deal of the Day (With live ticking countdown and claims progress bar)
            DealOfTheDaySection(products, navController)

            // 5. Trending Products
            ProductsGridSection(
                title = "Trending Now",
                products = products.filter { it.isFeatured }.take(8),
                wishlistItems = wishlistItems.map { it.productId },
                navController = navController,
                onToggleWishlist = { viewModel.toggleWishlist(it) },
                onAddToCart = { p ->
                    val size = p.getSizesList().firstOrNull()?.first ?: "M"
                    val color = p.getColorsList().firstOrNull()?.first ?: "Standard"
                    viewModel.addToCart(p.id, color, size, 1)
                    onShowSnackbar("${p.title} added to bag.")
                }
            )

            // 6. Promotional Banners
            PromotionalBannersSection(navController)

            // 7. New Arrivals
            ProductsGridSection(
                title = "New Arrivals",
                products = products.filter { it.isNewArrival }.take(4),
                wishlistItems = wishlistItems.map { it.productId },
                navController = navController,
                onToggleWishlist = { viewModel.toggleWishlist(it) },
                onAddToCart = { p ->
                    val size = p.getSizesList().firstOrNull()?.first ?: "M"
                    val color = p.getColorsList().firstOrNull()?.first ?: "Standard"
                    viewModel.addToCart(p.id, color, size, 1)
                    onShowSnackbar("${p.title} added to bag.")
                }
            )

            // 8. Brand Features Strip
            BrandFeaturesStrip()

            // 9. Customer Testimonials
            TestimonialsSection()

            // 10. Newsletter Subscription
            NewsletterSection(onShowSnackbar)

            // 11. Footer
            FooterSection(navController)
        }
    }
}

@Composable
fun AnnouncementBar() {
    val texts = listOf(
        "FREE SHIPPING ON ORDERS ABOVE ₹999",
        "EXTRA 10% OFF ON FIRST ORDER | USE CODE: FIRST10",
        "EASY 7-DAY RETURNS & EXCHANGES"
    )
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            currentIndex = (currentIndex + 1) % texts.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandDarkNavy)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = {
                slideInVertically { height -> height } + fadeIn() togetherWith
                        slideOutVertically { height -> -height } + fadeOut()
            },
            label = "AnnouncementCarousel"
        ) { index ->
            Text(
                text = texts[index],
                color = BrandWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

data class BannerSlide(
    val title: String,
    val subtitle: String,
    val buttonText: String,
    val bgColors: List<Color>,
    val imageUrl: String,
    val targetRoute: String
)

@Composable
fun HeroBannerSlider(navController: NavController) {
    val slides = listOf(
        BannerSlide(
            "Redefine Your Style",
            "Men's Premium Collection Launch",
            "Shop Now",
            listOf(BrandBlack, BrandDarkNavy),
            "https://placehold.co/800x400/000000/ffffff?text=RANGE+MEN+COLLECTION",
            "category/menswear"
        ),
        BannerSlide(
            "Fashion for Little Stars",
            "Playful, Safe & Super Comfy Outfits",
            "Explore",
            listOf(Color(0xFFEC4899), Color(0xFFF43F5E)),
            "https://placehold.co/800x400/EC4899/ffffff?text=RANGE+KIDS+WEAR",
            "category/kidswear"
        ),
        BannerSlide(
            "Step Into Comfort",
            "Premium Crafted Italian & Sport Footwear",
            "Shop Footwear",
            listOf(BrandDarkNavy, Color(0xFF334155)),
            "https://placehold.co/800x400/1D3557/ffffff?text=RANGE+FOOTWEAR",
            "category/footwear"
        ),
        BannerSlide(
            "UP TO 50% OFF",
            "Mega Seasonal Offer - Strictly Limited Time",
            "Shop Sale",
            listOf(BrandAccentRed, Color(0xFF991B1B)),
            "https://placehold.co/800x400/E63946/ffffff?text=RANGE+SEASONAL+SALE",
            "category/menswear"
        )
    )

    var currentSlideIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentSlideIndex = (currentSlideIndex + 1) % slides.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        val slide = slides[currentSlideIndex]

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(slide.bgColors))
        ) {
            AsyncImage(
                model = slide.imageUrl,
                contentDescription = slide.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.35f }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = slide.title.uppercase(),
                    color = BrandWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    lineHeight = 30.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = slide.subtitle,
                    color = BrandWhite.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate(slide.targetRoute) },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandWhite, contentColor = BrandBlack),
                    shape = RoundedCornerShape(0.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = slide.buttonText.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Indicator dots
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            slides.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (index == currentSlideIndex) BrandWhite else BrandWhite.copy(alpha = 0.4f))
                        .clickable { currentSlideIndex = index }
                )
            }
        }
    }
}

@Composable
fun CategoryCardsSection(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "SHOP BY CATEGORY",
            color = BrandBlack,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categories = listOf(
                Triple("MEN'S WEAR", "category/menswear", "https://placehold.co/300x400/1D3557/ffffff?text=MEN"),
                Triple("KIDS WEAR", "category/kidswear", "https://placehold.co/300x400/EC4899/ffffff?text=KIDS"),
                Triple("FOOTWEAR", "category/footwear", "https://placehold.co/300x400/000000/ffffff?text=SHOES")
            )

            categories.forEach { (name, route, imgUrl) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { navController.navigate(route) }
                ) {
                    AsyncImage(
                        model = imgUrl,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = name,
                            color = BrandWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Explore",
                            color = BrandWhite.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DealOfTheDaySection(products: List<Product>, navController: NavController) {
    if (products.isEmpty()) return

    // Find 4 items on heavy sale or just take first 4 and show big discounts
    val dealItems = products.filter { it.discountPercent >= 48 }.take(4)
    if (dealItems.isEmpty()) return

    // Countdown Timer logic (Targeting end of day UTC)
    var timeLeft by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            val endOfDay = calendar.timeInMillis
            val diff = endOfDay - now
            if (diff > 0) {
                val hours = (diff / (1000 * 60 * 60)) % 24
                val minutes = (diff / (1000 * 60)) % 60
                val seconds = (diff / 1000) % 60
                timeLeft = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                timeLeft = "00:00:00"
            }
            delay(1000)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandDarkNavy),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DEAL OF THE DAY",
                        color = BrandWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Hurry, offers ending soon",
                        color = BrandWhite.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }

                // Countdown display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(BrandAccentRed, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = BrandWhite,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeLeft,
                        color = BrandWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dealItems.forEach { product ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(BrandWhite)
                            .clickable { navController.navigate("detail/${product.id}") }
                            .padding(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = product.getImagesList().firstOrNull() ?: "",
                                contentDescription = product.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .background(BrandAccentRed)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .align(Alignment.TopStart)
                            ) {
                                Text(
                                    text = "${product.discountPercent}% OFF",
                                    color = BrandWhite,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.title,
                            color = BrandBlack,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "₹${product.sellingPrice.toInt()}",
                                color = BrandBlack,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "₹${product.mrp.toInt()}",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        // Urgency bar
                        val claimedPercent = remember { Random.nextInt(55, 88) }
                        LinearProgressIndicator(
                            progress = { claimedPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = BrandAccentRed,
                            trackColor = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$claimedPercent% Claimed",
                            color = BrandAccentRed,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductsGridSection(
    title: String,
    products: List<Product>,
    wishlistItems: List<Int>,
    navController: NavController,
    onToggleWishlist: (Int) -> Unit,
    onAddToCart: (Product) -> Unit
) {
    if (products.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title.uppercase(),
            color = BrandBlack,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            products.forEach { product ->
                ProductCard(
                    product = product,
                    isWishlisted = wishlistItems.contains(product.id),
                    onClick = { navController.navigate("detail/${product.id}") },
                    onToggleWishlist = { onToggleWishlist(product.id) },
                    onAddToCart = { onAddToCart(product) }
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    isWishlisted: Boolean,
    onClick: () -> Unit,
    onToggleWishlist: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = BrandWhite),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, BrandBorderGrey)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) {
                AsyncImage(
                    model = product.getImagesList().firstOrNull() ?: "",
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    if (product.isNewArrival) {
                        Box(
                            modifier = Modifier
                                .background(BrandBlack)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "NEW",
                                color = BrandWhite,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (product.isOnSale) {
                        Box(
                            modifier = Modifier
                                .background(BrandAccentRed)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "SALE",
                                color = BrandWhite,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(1.dp))
                    }

                    // Wishlist heart
                    IconButton(
                        onClick = onToggleWishlist,
                        modifier = Modifier
                            .size(28.dp)
                            .background(BrandWhite.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) BrandAccentRed else BrandBlack,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "RANGE",
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = product.title,
                    color = BrandBlack,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${product.sellingPrice.toInt()}",
                        color = BrandBlack,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "₹${product.mrp.toInt()}",
                        color = Color.Gray,
                        fontSize = 9.sp,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${product.discountPercent}% OFF)",
                        color = GreenDiscount,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${product.averageRating}",
                        color = BrandBlack,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "(${product.ratingCount})",
                        color = Color.Gray,
                        fontSize = 9.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    contentPadding = PaddingValues(0.dp),
                    border = BorderStroke(1.dp, BrandBlack),
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlack)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Add",
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ADD TO BAG", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PromotionalBannersSection(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Promo 1
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE2E8F0))
                    .clickable { navController.navigate("category/menswear") }
            ) {
                AsyncImage(
                    model = "https://placehold.co/400x200/1D3557/ffffff?text=MEN+T-SHIRTS",
                    contentDescription = "Tees",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("T-SHIRTS", color = BrandWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("STARTING ₹499", color = BrandWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("SHOP NOW", color = BrandWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
                }
            }

            // Promo 2
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFFCE7F3))
                    .clickable { navController.navigate("category/kidswear") }
            ) {
                AsyncImage(
                    model = "https://placehold.co/400x200/EC4899/ffffff?text=KIDS+COLLECTION",
                    contentDescription = "Kids under 799",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("KIDS SPECIAL", color = BrandWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("UNDER ₹799", color = BrandWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("EXPLORE", color = BrandWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Full width Promo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(BrandBlack)
                .clickable { navController.navigate("category/footwear") }
        ) {
            AsyncImage(
                model = "https://placehold.co/800x200/000000/ffffff?text=FOOTWEAR+FEST",
                contentDescription = "Footwear Fest",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "FOOTWEAR FEST",
                    color = BrandWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "FLAT 40% OFF",
                    color = BrandAccentRed,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun BrandFeaturesStrip() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandLightGrey)
            .padding(vertical = 20.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val features = listOf(
            "🚚" to "FREE SHIPPING",
            "🔄" to "7-DAY RETURNS",
            "💳" to "SECURE PAY",
            "⭐" to "Luxe Quality"
        )
        features.forEach { (emoji, text) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = text,
                    color = BrandBlack,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class Testimonial(
    val name: String,
    val text: String,
    val rating: Int
)

@Composable
fun TestimonialsSection() {
    val reviews = listOf(
        Testimonial("Vikram S.", "High-end fashion at a reasonable price! The oversized t-shirts fit perfectly and are amazingly heavy.", 5),
        Testimonial("Priya K.", "Ordered ethnic wear for my kid. Very soft fabric, beautiful design, and fast delivery too!", 5),
        Testimonial("Rohit M.", "Derived extreme comfort from the AeroStride sneakers. Will buy again from RANGE.", 4),
        Testimonial("Meera J.", "7-Day returns are really easy. Had an exchange on size and it got done in 3 days. Phenomenal customer care.", 5),
        Testimonial("Anil P.", "Authentic leather Oxfords are beautiful. RANGE has definitely become my default style destination.", 5)
    )

    var currentTestimonialIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(6000)
            currentTestimonialIndex = (currentTestimonialIndex + 1) % reviews.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "WHAT OUR CUSTOMERS SAY",
            color = BrandBlack,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            colors = CardDefaults.cardColors(containerColor = BrandWhite),
            border = BorderStroke(1.dp, BrandBorderGrey)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                val review = reviews[currentTestimonialIndex]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Stars
                    Row {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < review.rating) Color(0xFFFFB300) else Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "\"${review.text}\"",
                        color = BrandBlack,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "- ${review.name}",
                        color = BrandDarkNavy,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun NewsletterSection(onShowSnackbar: (String) -> Unit) {
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandDarkNavy)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "STAY IN THE RANGE",
            color = BrandWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Subscribe and get 10% off your first purchase",
            color = BrandWhite.copy(alpha = 0.8f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Enter your email address", fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BrandWhite,
                    unfocusedContainerColor = BrandWhite,
                    focusedTextColor = BrandBlack,
                    unfocusedTextColor = BrandBlack,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(0.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Button(
                onClick = {
                    if (email.contains("@") && email.contains(".")) {
                        onShowSnackbar("Subscribed successfully! Check your inbox.")
                        email = ""
                    } else {
                        onShowSnackbar("Please enter a valid email address.")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandAccentRed),
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text("SUBSCRIBE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FooterSection(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandBlack)
            .padding(24.dp)
    ) {
        Text(
            text = "RANGE",
            color = BrandWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.sp
        )
        Text(
            text = "WEAR THE RANGE",
            color = BrandAccentRed,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Modern, Minimal, Premium e-commerce clothing and footwear brand designed for people who stand out.",
            color = Color.LightGray,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quick navigation links
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("QUICK LINKS", color = BrandWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                val links = listOf(
                    "About Us" to "about",
                    "Contact Us" to "contact",
                    "FAQs" to "faq"
                )
                links.forEach { (label, route) ->
                    Text(
                        text = label,
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clickable { navController.navigate(route) }
                            .padding(vertical = 4.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("CATEGORIES", color = BrandWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                val cats = listOf(
                    "Men's Collection" to "category/menswear",
                    "Kids Special" to "category/kidswear",
                    "Premium Footwear" to "category/footwear"
                )
                cats.forEach { (label, route) ->
                    Text(
                        text = label,
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clickable { navController.navigate(route) }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider(color = Color.DarkGray)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "© 2026 RANGE. All Rights Reserved.",
            color = Color.Gray,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
