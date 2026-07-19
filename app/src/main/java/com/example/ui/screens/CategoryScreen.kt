package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.Product
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categoryName: String, // "menswear", "kidswear", "footwear"
    navController: NavController,
    viewModel: MainViewModel,
    onShowSnackbar: (String) -> Unit
) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val wishlistItems by viewModel.wishlistItems.collectAsStateWithLifecycle()

    // Filter states from view model
    val selectedSubcategories by viewModel.selectedSubcategories.collectAsStateWithLifecycle()
    val priceRange by viewModel.priceRange.collectAsStateWithLifecycle()
    val selectedSizes by viewModel.selectedSizes.collectAsStateWithLifecycle()
    val selectedColors by viewModel.selectedColors.collectAsStateWithLifecycle()
    val selectedDiscount by viewModel.selectedDiscount.collectAsStateWithLifecycle()
    val selectedRating by viewModel.selectedRating.collectAsStateWithLifecycle()
    val sortType by viewModel.sortType.collectAsStateWithLifecycle()

    // Local state
    var isListView by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    // Reset filters when switching category
    LaunchedEffect(categoryName) {
        viewModel.clearAllFilters()
    }

    // Filter products
    val filteredProducts = remember(products, categoryName, selectedSubcategories, priceRange, selectedSizes, selectedColors, selectedDiscount, selectedRating, sortType) {
        var result = products.filter { it.category.lowercase() == categoryName.lowercase() }

        // Subcategory filter
        if (selectedSubcategories.isNotEmpty()) {
            result = result.filter { selectedSubcategories.contains(it.subcategory) }
        }

        // Price filter
        result = result.filter { it.sellingPrice in priceRange }

        // Size filter
        if (selectedSizes.isNotEmpty()) {
            result = result.filter { p ->
                p.getSizesList().any { selectedSizes.contains(it.first) }
            }
        }

        // Color filter
        if (selectedColors.isNotEmpty()) {
            result = result.filter { p ->
                p.getColorsList().any { selectedColors.contains(it.first) }
            }
        }

        // Discount filter
        if (selectedDiscount != null) {
            result = result.filter { it.discountPercent >= selectedDiscount!! }
        }

        // Rating filter
        if (selectedRating != null) {
            result = result.filter { it.averageRating >= selectedRating!! }
        }

        // Sort
        result = when (sortType) {
            "PriceLowToHigh" -> result.sortedBy { it.sellingPrice }
            "PriceHighToLow" -> result.sortedByDescending { it.sellingPrice }
            "Rating" -> result.sortedByDescending { it.averageRating }
            "Popularity" -> result.sortedByDescending { it.ratingCount }
            "Discount" -> result.sortedByDescending { it.discountPercent }
            else -> result // Relevance
        }

        result
    }

    // Get all subcategories for the current category to display in filter check list
    val availableSubcategories = remember(products, categoryName) {
        products.filter { it.category.lowercase() == categoryName.lowercase() }
            .map { it.subcategory }
            .distinct()
            .sorted()
    }

    Scaffold(
        topBar = {
            Column {
                // Breadcrumb and Title
                val displayTitle = when (categoryName.lowercase()) {
                    "menswear" -> "MEN'S WEAR"
                    "kidswear" -> "KIDS WEAR"
                    "footwear" -> "PREMIUM FOOTWEAR"
                    else -> categoryName.uppercase()
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandWhite)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        // Breadcrumbs
                        Text(
                            text = "Home > $displayTitle",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = displayTitle,
                            color = BrandBlack,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Text(
                        text = "${filteredProducts.size} Products",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Control panel (Sort, View toggle, Filter button)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandLightGrey)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filter button
                    Button(
                        onClick = { showFilterSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlack, contentColor = BrandWhite),
                        shape = RoundedCornerShape(0.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = "Filters",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("FILTERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Sort dropdown trigger
                        var showSortMenu by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { showSortMenu = true },
                                border = BorderStroke(1.dp, BrandBorderGrey),
                                shape = RoundedCornerShape(0.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlack)
                            ) {
                                Text(
                                    text = "SORT BY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(BrandWhite)
                            ) {
                                val sorts = listOf(
                                    "Relevance" to "Relevance",
                                    "Price: Low to High" to "PriceLowToHigh",
                                    "Price: High to Low" to "PriceHighToLow",
                                    "Customer Rating" to "Rating",
                                    "Bestselling / Popularity" to "Popularity",
                                    "Better Discount" to "Discount"
                                )
                                sorts.forEach { (label, value) ->
                                    DropdownMenuItem(
                                        text = { Text(label, fontSize = 12.sp, color = BrandBlack) },
                                        onClick = {
                                            viewModel.setSortType(value)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Grid/List Toggle
                        IconButton(
                            onClick = { isListView = !isListView },
                            modifier = Modifier
                                .border(1.dp, BrandBorderGrey)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isListView) Icons.Outlined.GridView else Icons.Outlined.ViewList,
                                contentDescription = "Toggle View",
                                tint = BrandBlack,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (filteredProducts.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = "No Products",
                        tint = Color.LightGray,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No products found",
                        color = BrandBlack,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try adjusting or clearing your filters to see more collection.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.clearAllFilters() },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlack),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("CLEAR ALL FILTERS", fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                    }
                }
            } else {
                if (isListView) {
                    // List View Layout
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredProducts) { product ->
                            ListProductCard(
                                product = product,
                                isWishlisted = wishlistItems.any { it.productId == product.id },
                                onClick = { navController.navigate("detail/${product.id}") },
                                onToggleWishlist = { viewModel.toggleWishlist(product.id) },
                                onAddToCart = {
                                    val firstSize = product.getSizesList().firstOrNull()?.first ?: "M"
                                    val firstColor = product.getColorsList().firstOrNull()?.first ?: "Standard"
                                    viewModel.addToCart(product.id, firstColor, firstSize, 1)
                                    onShowSnackbar("${product.title} added to bag.")
                                }
                            )
                        }
                    }
                } else {
                    // Grid View Layout
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredProducts) { product ->
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                ProductCard(
                                    product = product,
                                    isWishlisted = wishlistItems.any { it.productId == product.id },
                                    onClick = { navController.navigate("detail/${product.id}") },
                                    onToggleWishlist = { viewModel.toggleWishlist(product.id) },
                                    onAddToCart = {
                                        val firstSize = product.getSizesList().firstOrNull()?.first ?: "M"
                                        val firstColor = product.getColorsList().firstOrNull()?.first ?: "Standard"
                                        viewModel.addToCart(product.id, firstColor, firstSize, 1)
                                        onShowSnackbar("${product.title} added to bag.")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Collapsible Filters Sheet
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
                containerColor = BrandWhite,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                FilterSheetContent(
                    availableSubcategories = availableSubcategories,
                    selectedSubcategories = selectedSubcategories,
                    onToggleSubcategory = { viewModel.toggleSubcategoryFilter(it) },
                    priceRange = priceRange,
                    onPriceRangeChange = { viewModel.setPriceRangeFilter(it) },
                    selectedSizes = selectedSizes,
                    onToggleSize = { viewModel.toggleSizeFilter(it) },
                    selectedColors = selectedColors,
                    onToggleColor = { viewModel.toggleColorFilter(it) },
                    selectedDiscount = selectedDiscount,
                    onSelectDiscount = { viewModel.setDiscountFilter(it) },
                    selectedRating = selectedRating,
                    onSelectRating = { viewModel.setRatingFilter(it) },
                    onClearAll = {
                        viewModel.clearAllFilters()
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) showFilterSheet = false
                        }
                    },
                    onApply = {
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) showFilterSheet = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ListProductCard(
    product: Product,
    isWishlisted: Boolean,
    onClick: () -> Unit,
    onToggleWishlist: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = BrandWhite),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, BrandBorderGrey)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
            ) {
                AsyncImage(
                    model = product.getImagesList().firstOrNull() ?: "",
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = onToggleWishlist,
                    modifier = Modifier
                        .size(24.dp)
                        .background(BrandWhite.copy(alpha = 0.8f), CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isWishlisted) BrandAccentRed else BrandBlack,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
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
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.description,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 13.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${product.sellingPrice.toInt()}",
                        color = BrandBlack,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "₹${product.mrp.toInt()}",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${product.discountPercent}% OFF)",
                        color = GreenDiscount,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlack, contentColor = BrandWhite)
                ) {
                    Text("ADD TO BAG", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterSheetContent(
    availableSubcategories: List<String>,
    selectedSubcategories: Set<String>,
    onToggleSubcategory: (String) -> Unit,
    priceRange: ClosedFloatingPointRange<Float>,
    onPriceRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    selectedSizes: Set<String>,
    onToggleSize: (String) -> Unit,
    selectedColors: Set<String>,
    onToggleColor: (String) -> Unit,
    selectedDiscount: Int?,
    onSelectDiscount: (Int?) -> Unit,
    selectedRating: Float?,
    onSelectRating: (Float?) -> Unit,
    onClearAll: () -> Unit,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 500.dp)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filters", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = BrandBlack)
            Text(
                text = "Clear All",
                fontSize = 13.sp,
                color = BrandAccentRed,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onClearAll() }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Subcategories Check list
        if (availableSubcategories.isNotEmpty()) {
            Text("Category", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableSubcategories.forEach { sub ->
                    val isSelected = selectedSubcategories.contains(sub)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToggleSubcategory(sub) },
                        label = { Text(sub, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandBlack,
                            selectedLabelColor = BrandWhite,
                            containerColor = BrandLightGrey,
                            labelColor = BrandBlack
                        ),
                        border = null,
                        shape = RoundedCornerShape(2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 2. Price Slider
        Text("Price Range (₹0 - ₹10,000)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
        Spacer(modifier = Modifier.height(8.dp))
        RangeSlider(
            value = priceRange.start..priceRange.endInclusive,
            onValueChange = { onPriceRangeChange(it) },
            valueRange = 0f..10000f,
            steps = 20,
            colors = SliderDefaults.colors(
                activeTrackColor = BrandBlack,
                inactiveTrackColor = Color.LightGray,
                thumbColor = BrandBlack
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("₹${priceRange.start.toInt()}", fontSize = 11.sp, color = Color.Gray)
            Text("₹${priceRange.endInclusive.toInt()}", fontSize = 11.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Size Filter
        Text("Sizes", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
        Spacer(modifier = Modifier.height(6.dp))
        val sizes = listOf("S", "M", "L", "XL", "XXL", "7", "8", "9", "10", "11")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            sizes.forEach { size ->
                val isSelected = selectedSizes.contains(size)
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggleSize(size) },
                    label = { Text(size, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandBlack,
                        selectedLabelColor = BrandWhite,
                        containerColor = BrandLightGrey,
                        labelColor = BrandBlack
                    ),
                    border = null,
                    shape = RoundedCornerShape(2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Color Filters
        Text("Colors", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
        Spacer(modifier = Modifier.height(6.dp))
        val colorsMap = listOf(
            "White" to Color(0xFFFFFFFF),
            "Black" to Color(0xFF000000),
            "Classic Blue" to Color(0xFF1D3557),
            "Grey" to Color(0xFF8E8E93),
            "Pink" to Color(0xFFEC4899),
            "Red" to Color(0xFFE63946),
            "Green" to Color(0xFF2E7D32),
            "Tan" to Color(0xFF78350F)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            colorsMap.forEach { (name, colorValue) ->
                val isSelected = selectedColors.contains(name)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colorValue)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) BrandAccentRed else Color.LightGray,
                            shape = CircleShape
                        )
                        .clickable { onToggleColor(name) },
                    contentAlignment = Alignment.Center
                ) {
                    if (name == "White") {
                        // Tiny dot for white
                        Box(modifier = Modifier.size(4.dp).background(Color.Gray, CircleShape))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Discount
        Text("Discounts", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val discounts = listOf(10, 20, 30, 50)
            discounts.forEach { disc ->
                val isSelected = selectedDiscount == disc
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectDiscount(if (isSelected) null else disc) },
                    label = { Text("$disc%+ OFF", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandBlack,
                        selectedLabelColor = BrandWhite,
                        containerColor = BrandLightGrey,
                        labelColor = BrandBlack
                    ),
                    border = null,
                    shape = RoundedCornerShape(2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Ratings
        Text("Ratings", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val ratings = listOf(4.0f, 3.0f, 2.0f)
            ratings.forEach { rating ->
                val isSelected = selectedRating == rating
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectRating(if (isSelected) null else rating) },
                    label = { Text("${rating.toInt()}★ & Above", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandBlack,
                        selectedLabelColor = BrandWhite,
                        containerColor = BrandLightGrey,
                        labelColor = BrandBlack
                    ),
                    border = null,
                    shape = RoundedCornerShape(2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onApply,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlack, contentColor = BrandWhite),
            shape = RoundedCornerShape(0.dp)
        ) {
            Text("APPLY FILTERS", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}
