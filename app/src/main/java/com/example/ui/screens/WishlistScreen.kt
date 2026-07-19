package com.example.ui.screens

import kotlinx.coroutines.delay

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
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

@Composable
fun WishlistScreen(
    navController: NavController,
    viewModel: MainViewModel,
    onShowSnackbar: (String) -> Unit
) {
    val wishlistItems by viewModel.wishlistItems.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()

    // Get the actual product lists that are wishlisted
    val wishlistedProducts = remember(wishlistItems, products) {
        val ids = wishlistItems.map { it.productId }
        products.filter { ids.contains(it.id) }
    }

    var isScreenLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1000)
        isScreenLoading = false
    }

    if (isScreenLoading) {
        WishlistScreenSkeleton()
    } else if (wishlistedProducts.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Empty Wishlist",
                tint = Color.LightGray,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "YOUR WISHLIST IS EMPTY",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandBlack,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap on heart icons across our categories to save styles you love.",
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { navController.navigate("home") },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlack, contentColor = BrandWhite),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text("EXPLORE PRODUCTS", fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandLightGrey)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandWhite)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MY WISHLIST (${wishlistedProducts.size} items)",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandBlack,
                letterSpacing = 1.sp
            )

            TextButton(
                onClick = {
                    wishlistedProducts.forEach { product ->
                        val size = product.getSizesList().firstOrNull()?.first ?: "M"
                        val color = product.getColorsList().firstOrNull()?.first ?: "Standard"
                        viewModel.addToCart(product.id, color, size, 1)
                        viewModel.toggleWishlist(product.id)
                    }
                    onShowSnackbar("Moved all items to bag!")
                }
            ) {
                Text("MOVE ALL TO BAG", color = BrandAccentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(wishlistedProducts) { product ->
                WishlistProductCard(
                    product = product,
                    onClick = { navController.navigate("detail/${product.id}") },
                    onRemove = {
                        viewModel.toggleWishlist(product.id)
                        onShowSnackbar("Removed from wishlist.")
                    },
                    onAddToCart = { size ->
                        val color = product.getColorsList().firstOrNull()?.first ?: "Standard"
                        viewModel.addToCart(product.id, color, size, 1)
                        viewModel.toggleWishlist(product.id)
                        onShowSnackbar("Moved ${product.title} to bag!")
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistProductCard(
    product: Product,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onAddToCart: (String) -> Unit
) {
    val sizes = product.getSizesList()
    var selectedSize by remember { mutableStateOf(sizes.firstOrNull()?.first ?: "M") }
    var sizeExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = BrandWhite),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, BrandBorderGrey)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = product.getImagesList().firstOrNull() ?: "",
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Close (X) button
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(26.dp)
                        .background(BrandWhite.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Remove",
                        tint = BrandBlack,
                        modifier = Modifier.size(14.dp)
                    )
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
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Interactive Size Dropdown Selector before moving to bag
                if (sizes.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { sizeExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp),
                            border = BorderStroke(1.dp, BrandBorderGrey),
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlack)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Size: $selectedSize", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }

                        DropdownMenu(
                            expanded = sizeExpanded,
                            onDismissRequest = { sizeExpanded = false },
                            modifier = Modifier.background(BrandWhite)
                        ) {
                            sizes.forEach { (size, stock) ->
                                val isOutOfStock = stock <= 0
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (isOutOfStock) "$size (Out of stock)" else size,
                                            fontSize = 11.sp,
                                            color = if (isOutOfStock) Color.LightGray else BrandBlack
                                        )
                                    },
                                    onClick = {
                                        selectedSize = size
                                        sizeExpanded = false
                                    },
                                    enabled = !isOutOfStock
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Button(
                    onClick = { onAddToCart(selectedSize) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
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
