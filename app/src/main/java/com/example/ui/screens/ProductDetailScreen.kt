package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.Product
import com.example.data.Review
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun ProductDetailScreen(
    productId: Int,
    navController: NavController,
    viewModel: MainViewModel,
    onShowSnackbar: (String) -> Unit
) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val product = products.find { it.id == productId }

    val wishlistItems by viewModel.wishlistItems.collectAsStateWithLifecycle()
    val isWishlisted = wishlistItems.any { it.productId == productId }

    val reviewsFlow = remember(productId) { viewModel.getReviewsForProduct(productId) }
    val reviewsList by reviewsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val recentlyViewedIds by viewModel.recentlyViewedIds.collectAsStateWithLifecycle()

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandBlack)
        }
        return
    }

    // Add to recently viewed
    LaunchedEffect(productId) {
        viewModel.addToRecentlyViewed(productId)
    }

    // States for selections
    var selectedImageIndex by remember { mutableIntStateOf(0) }
    var selectedColor by remember { mutableStateOf(product.getColorsList().firstOrNull()?.first ?: "") }
    var selectedSize by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }

    // Accordion states
    var showDesc by remember { mutableStateOf(true) }
    var showSpecs by remember { mutableStateOf(false) }
    var showCare by remember { mutableStateOf(false) }

    // Delivery check state
    var pincode by remember { mutableStateOf("") }
    var deliveryEstimate by remember { mutableStateOf("") }

    // Dialog state
    var showSizeChart by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandWhite)
            .verticalScroll(scrollState)
    ) {
        // Breadcrumb
        Text(
            text = "Home > ${product.category.uppercase()} > ${product.subcategory} > ${product.title}",
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Medium
        )

        // 1. Image Area
        val images = product.getImagesList()
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                AsyncImage(
                    model = images.getOrNull(selectedImageIndex) ?: "",
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Favorite button floating on image
                IconButton(
                    onClick = { viewModel.toggleWishlist(product.id) },
                    modifier = Modifier
                        .padding(16.dp)
                        .size(40.dp)
                        .background(BrandWhite.copy(alpha = 0.8f), CircleShape)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isWishlisted) BrandAccentRed else BrandBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Thumbnail Gallery
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                images.forEachIndexed { index, url ->
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .border(
                                width = if (selectedImageIndex == index) 2.dp else 1.dp,
                                color = if (selectedImageIndex == index) BrandBlack else BrandBorderGrey,
                                shape = RoundedCornerShape(2.dp)
                            )
                            .clickable { selectedImageIndex = index }
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = "Thumbnail $index",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // 2. Info Area
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "RANGE",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = product.title,
                color = BrandBlack,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 24.sp
            )

            // Rating summary line
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(BrandDarkNavy, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${product.averageRating}", color = BrandWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(11.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${product.ratingCount} Ratings & Reviews",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        // Scroll to reviews (approximate by scrolling down)
                    }
                )
            }

            HorizontalDivider(color = BrandBorderGrey, modifier = Modifier.padding(vertical = 12.dp))

            // Price section
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "₹${product.sellingPrice.toInt()}",
                    color = BrandBlack,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MRP ₹${product.mrp.toInt()}",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.LineThrough,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${product.discountPercent}% OFF)",
                    color = GreenDiscount,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            Text("inclusive of all taxes", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))

            // Promotional/Bank Offers Accordion
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandLightGrey),
                shape = RoundedCornerShape(4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("AVAILABLE OFFERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandBlack, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("🏷️ ", fontSize = 12.sp)
                        Text("Bank Offer: 10% instant discount on HDFC credit cards.", fontSize = 11.sp, color = BrandBlack)
                    }
                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("🏷️ ", fontSize = 12.sp)
                        Text("Coupon: Use code RANGE100 for flat ₹100 discount above ₹999.", fontSize = 11.sp, color = BrandBlack)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Selection controls
            // ColorsSwatches
            val colors = product.getColorsList()
            if (colors.isNotEmpty()) {
                Text("Select Color: $selectedColor", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colors.forEach { (name, hex) ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .border(
                                    width = if (selectedColor == name) 3.dp else 1.dp,
                                    color = if (selectedColor == name) BrandAccentRed else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = name }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Sizes swatches
            val sizes = product.getSizesList()
            if (sizes.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Size", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
                    Text(
                        "Size Chart",
                        fontSize = 11.sp,
                        color = BrandAccentRed,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { showSizeChart = true }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sizes.forEach { (size, stock) ->
                        val isSelected = selectedSize == size
                        val isOutOfStock = stock <= 0
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isOutOfStock) Color.LightGray else if (isSelected) BrandBlack else BrandBorderGrey,
                                    shape = RoundedCornerShape(2.dp)
                                )
                                .background(if (isSelected) BrandBlack else if (isOutOfStock) BrandLightGrey else BrandWhite)
                                .clickable(enabled = !isOutOfStock) { selectedSize = size },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                size,
                                color = if (isSelected) BrandWhite else if (isOutOfStock) Color.LightGray else BrandBlack,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (isOutOfStock) TextDecoration.LineThrough else null
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Quantity selector
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Quantity: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
                Spacer(modifier = Modifier.width(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.border(1.dp, BrandBorderGrey, RoundedCornerShape(4.dp))
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                    }
                    Text(
                        "$quantity",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    IconButton(
                        onClick = { if (quantity < 10) quantity++ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ADD TO CART
                Button(
                    onClick = {
                        if (sizes.isNotEmpty() && selectedSize.isEmpty()) {
                            onShowSnackbar("Please select a size first.")
                        } else {
                            viewModel.addToCart(product.id, selectedColor, selectedSize, quantity)
                            onShowSnackbar("Added $quantity item(s) to your bag!")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlack, contentColor = BrandWhite)
                ) {
                    Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ADD TO BAG", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                // BUY NOW
                Button(
                    onClick = {
                        if (sizes.isNotEmpty() && selectedSize.isEmpty()) {
                            onShowSnackbar("Please select a size first.")
                        } else {
                            viewModel.addToCart(product.id, selectedColor, selectedSize, quantity)
                            navController.navigate("cart")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandAccentRed, contentColor = BrandWhite)
                ) {
                    Text("BUY NOW", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pincode Delivery check
            Text("Delivery Check", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = pincode,
                    onValueChange = { pincode = it },
                    placeholder = { Text("Enter Pincode", fontSize = 11.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(0.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BrandBlack,
                        unfocusedTextColor = BrandBlack,
                        focusedBorderColor = BrandBlack,
                        unfocusedBorderColor = BrandBorderGrey
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (pincode.length == 6 && pincode.toIntOrNull() != null) {
                            deliveryEstimate = "Delivery by next Saturday, Dec 28."
                        } else {
                            onShowSnackbar("Please enter a valid 6-digit PIN code.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlack),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text("CHECK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (deliveryEstimate.isNotEmpty()) {
                Text(
                    text = "🚚 $deliveryEstimate",
                    color = GreenDiscount,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Specifications expandable accordions
            HorizontalDivider(color = BrandBorderGrey)

            // Description Dropdown
            DropdownRow(title = "Product Description", isOpen = showDesc, onToggle = { showDesc = !showDesc }) {
                Text(
                    text = product.description,
                    color = Color.DarkGray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            HorizontalDivider(color = BrandBorderGrey)

            // Detailed specifications
            DropdownRow(title = "Specifications", isOpen = showSpecs, onToggle = { showSpecs = !showSpecs }) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    SpecItem("Material", product.material)
                    SpecItem("Fit Type", product.fit)
                    SpecItem("Neck", product.neckType)
                    SpecItem("Sleeve Length", product.sleeveType)
                    SpecItem("Pattern", product.pattern)
                    SpecItem("Occasion", product.occasion)
                }
            }

            HorizontalDivider(color = BrandBorderGrey)

            // Care instructions
            DropdownRow(title = "Material & Care Instructions", isOpen = showCare, onToggle = { showCare = !showCare }) {
                Text(
                    text = product.careInstructions,
                    color = Color.DarkGray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            HorizontalDivider(color = BrandBorderGrey)
        }

        // 4. Ratings and reviews section
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "RATINGS & REVIEWS",
                color = BrandBlack,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Overall Summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 24.dp)
                ) {
                    Text(
                        text = "${product.averageRating}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandBlack
                    )
                    Row {
                        repeat(5) { i ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i < product.averageRating.toInt()) Color(0xFFFFB300) else Color.LightGray,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${product.ratingCount} reviews",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                // Bar breakdown
                Column(modifier = Modifier.weight(1f)) {
                    RatingBarRow(5, 60)
                    RatingBarRow(4, 20)
                    RatingBarRow(3, 10)
                    RatingBarRow(2, 5)
                    RatingBarRow(1, 5)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showReviewDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(0.dp),
                border = BorderStroke(1.dp, BrandBlack),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlack)
            ) {
                Text("WRITE A REVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reviews List
            reviewsList.forEach { review ->
                ReviewItem(review)
                HorizontalDivider(color = BrandBorderGrey, modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        // 5. You Might Also Like Screen
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                text = "YOU MIGHT ALSO LIKE",
                color = BrandBlack,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            )

            val related = products.filter { it.category == product.category && it.id != product.id }.take(6)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(related) { p ->
                    ProductCard(
                        product = p,
                        isWishlisted = wishlistItems.any { it.productId == p.id },
                        onClick = { navController.navigate("detail/${p.id}") },
                        onToggleWishlist = { viewModel.toggleWishlist(p.id) },
                        onAddToCart = {
                            val size = p.getSizesList().firstOrNull()?.first ?: "M"
                            val color = p.getColorsList().firstOrNull()?.first ?: "Standard"
                            viewModel.addToCart(p.id, color, size, 1)
                            onShowSnackbar("${p.title} added to bag.")
                        }
                    )
                }
            }
        }

        // 6. Recently Viewed Section
        val viewedProducts = products.filter { recentlyViewedIds.contains(it.id) && it.id != product.id }.take(4)
        if (viewedProducts.isNotEmpty()) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    text = "RECENTLY VIEWED",
                    color = BrandBlack,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewedProducts) { p ->
                        ProductCard(
                            product = p,
                            isWishlisted = wishlistItems.any { it.productId == p.id },
                            onClick = { navController.navigate("detail/${p.id}") },
                            onToggleWishlist = { viewModel.toggleWishlist(p.id) },
                            onAddToCart = {
                                val size = p.getSizesList().firstOrNull()?.first ?: "M"
                                val color = p.getColorsList().firstOrNull()?.first ?: "Standard"
                                viewModel.addToCart(p.id, color, size, 1)
                                onShowSnackbar("${p.title} added to bag.")
                            }
                        )
                    }
                }
            }
        }
    }

    // Size Chart modal
    if (showSizeChart) {
        Dialog(onDismissRequest = { showSizeChart = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Size Chart (Inches)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
                        IconButton(onClick = { showSizeChart = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Measurement table
                    SizeRow("Size", "Chest", "Waist", "Length")
                    HorizontalDivider()
                    SizeRow("S", "38", "32", "27")
                    SizeRow("M", "40", "34", "28")
                    SizeRow("L", "42", "36", "29")
                    SizeRow("XL", "44", "38", "30")
                    SizeRow("XXL", "46", "40", "31")

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showSizeChart = false },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlack),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CLOSE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Review Submit Dialog
    if (showReviewDialog) {
        var userRating by remember { mutableIntStateOf(5) }
        var reviewText by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showReviewDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Write a Review", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Stars selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(5) { i ->
                            val starRating = i + 1
                            IconButton(onClick = { userRating = starRating }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (starRating <= userRating) Color(0xFFFFB300) else Color.LightGray,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        placeholder = { Text("What did you like or dislike? How was the fit?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BrandBlack,
                            unfocusedTextColor = BrandBlack,
                            focusedBorderColor = BrandBlack,
                            unfocusedBorderColor = BrandBorderGrey
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showReviewDialog = false },
                            shape = RoundedCornerShape(0.dp),
                            border = BorderStroke(1.dp, BrandBlack),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CANCEL", color = BrandBlack)
                        }

                        Button(
                            onClick = {
                                if (reviewText.trim().isNotEmpty()) {
                                    viewModel.addReview(product.id, userRating, reviewText.trim())
                                    onShowSnackbar("Review submitted! Thank you.")
                                    showReviewDialog = false
                                } else {
                                    onShowSnackbar("Please write a comments review.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlack),
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("SUBMIT")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SizeRow(col1: String, col2: String, col3: String, col4: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(col1, fontWeight = FontWeight.Bold, color = BrandBlack, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(col2, color = Color.DarkGray, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(col3, color = Color.DarkGray, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(col4, color = Color.DarkGray, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
    }
}

@Composable
fun DropdownRow(title: String, isOpen: Boolean, onToggle: () -> Unit, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
            Icon(
                imageVector = if (isOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = BrandBlack
            )
        }

        AnimatedVisibility(
            visible = isOpen,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            content()
        }
    }
}

@Composable
fun SpecItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(value, color = BrandBlack, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

@Composable
fun RatingBarRow(stars: Int, percentage: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${stars}★", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.width(24.dp))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(CircleShape),
            color = Color(0xFFFFB300),
            trackColor = BrandBorderGrey
        )
        Text("$percentage%", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
    }
}

@Composable
fun ReviewItem(review: Review) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(review.userName, fontWeight = FontWeight.Bold, color = BrandBlack, fontSize = 12.sp)

            // Star Rating row
            Row {
                repeat(5) { i ->
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i < review.rating) Color(0xFFFFB300) else Color.LightGray,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }

        // Date
        val formattedDate = remember(review.reviewDate) {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            sdf.format(java.util.Date(review.reviewDate))
        }
        Text(formattedDate, color = Color.Gray, fontSize = 9.sp)

        Spacer(modifier = Modifier.height(4.dp))

        Text(review.text, color = Color.DarkGray, fontSize = 11.sp, lineHeight = 14.sp)
    }
}
