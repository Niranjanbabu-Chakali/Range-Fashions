package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
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
import com.example.data.CartItem
import com.example.data.Product
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun CartScreen(
    navController: NavController,
    viewModel: MainViewModel,
    onShowSnackbar: (String) -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val couponsList by viewModel.coupons.collectAsStateWithLifecycle()

    var showCouponDropdown by remember { mutableStateOf(false) }
    var couponInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    if (cartItems.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCartCheckout,
                contentDescription = "Empty Bag",
                tint = Color.LightGray,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "YOUR BAG IS EMPTY",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandBlack,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Add items from our premium collections and redefine your style.",
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
                Text("CONTINUE SHOPPING", fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
            }
        }
        return
    }

    // Calculations
    val totals = remember(cartItems, products, appliedCoupon) {
        var subtotal = 0.0
        var mrpTotal = 0.0

        cartItems.forEach { item ->
            val p = products.find { it.id == item.productId }
            if (p != null) {
                subtotal += (p.sellingPrice * item.quantity)
                mrpTotal += (p.mrp * item.quantity)
            }
        }

        val discount = mrpTotal - subtotal
        val deliveryCharge = if (subtotal >= 999.0 || subtotal == 0.0) 0.0 else 40.0

        var couponDiscount = 0.0
        appliedCoupon?.let { coupon ->
            if (subtotal >= coupon.minOrderAmount) {
                couponDiscount = if (coupon.discountType == "flat") {
                    coupon.discountValue
                } else {
                    val calculated = (subtotal * coupon.discountValue) / 100.0
                    if (calculated > coupon.maxDiscount) coupon.maxDiscount else calculated
                }
            }
        }

        val finalTotal = subtotal - couponDiscount + deliveryCharge
        val savedAmount = discount + couponDiscount

        CartTotals(
            mrpTotal = mrpTotal,
            subtotal = subtotal,
            discount = discount,
            deliveryCharge = deliveryCharge,
            couponDiscount = couponDiscount,
            finalTotal = finalTotal,
            savedAmount = savedAmount
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandLightGrey)
            .verticalScroll(scrollState)
    ) {
        // Cart Title Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandWhite)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SHOPPING BAG (${cartItems.size} items)",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandBlack,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cart items list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            cartItems.forEach { item ->
                val product = products.find { it.id == item.productId }
                if (product != null) {
                    CartItemCard(
                        item = item,
                        product = product,
                        onUpdateQty = { viewModel.updateCartQty(item.id, it) },
                        onRemove = {
                            viewModel.removeFromCart(item.id)
                            onShowSnackbar("${product.title} removed from bag.")
                        },
                        onMoveToWishlist = {
                            viewModel.toggleWishlist(product.id)
                            viewModel.removeFromCart(item.id)
                            onShowSnackbar("Moved ${product.title} to wishlist.")
                        },
                        onClick = { navController.navigate("detail/${product.id}") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Coupons section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(BrandWhite)
                .padding(16.dp)
        ) {
            Text(
                text = "APPLY COUPON",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlack,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = couponInput,
                    onValueChange = { couponInput = it },
                    placeholder = { Text("Enter Coupon Code", fontSize = 11.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(0.dp),
                    singleLine = true,
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
                        val errMsg = viewModel.applyCoupon(couponInput, totals.subtotal)
                        if (errMsg != null) {
                            onShowSnackbar(errMsg)
                        } else {
                            onShowSnackbar("Coupon applied successfully!")
                            couponInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlack),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text("APPLY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Show Coupon dropdown toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCouponDropdown = !showCouponDropdown }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (showCouponDropdown) "Hide Available Coupons" else "View Available Coupons",
                    color = BrandAccentRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (showCouponDropdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = BrandAccentRed,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = showCouponDropdown) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    couponsList.forEach { coupon ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BrandBorderGrey)
                                .background(BrandLightGrey)
                                .clickable {
                                    val errMsg = viewModel.applyCoupon(coupon.code, totals.subtotal)
                                    if (errMsg != null) {
                                        onShowSnackbar(errMsg)
                                    } else {
                                        onShowSnackbar("Coupon applied!")
                                        showCouponDropdown = false
                                    }
                                }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .background(BrandDarkNavy)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(coupon.code, color = BrandWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(coupon.description, color = Color.Gray, fontSize = 10.sp)
                            }
                            Text(
                                "USE",
                                color = BrandAccentRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Green applied coupon tag
            if (appliedCoupon != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8F5E9))
                        .border(1.dp, Color(0xFFC8E6C9))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Applied",
                            tint = GreenDiscount,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "'${appliedCoupon!!.code}' applied (Saved ₹${totals.couponDiscount.toInt()})",
                            color = GreenDiscount,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = { viewModel.removeCoupon() },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Remove coupon",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Price details Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(BrandWhite)
                .padding(16.dp)
        ) {
            Text(
                text = "PRICE DETAILS (${cartItems.size} items)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlack,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            PriceRowItem("Total MRP", "₹${totals.mrpTotal.toInt()}")
            PriceRowItem("Discount on MRP", "-₹${totals.discount.toInt()}", valueColor = GreenDiscount)
            if (appliedCoupon != null) {
                PriceRowItem("Coupon Discount", "-₹${totals.couponDiscount.toInt()}", valueColor = GreenDiscount)
            }
            PriceRowItem(
                label = "Delivery Charges",
                value = if (totals.deliveryCharge == 0.0) "FREE" else "₹${totals.deliveryCharge.toInt()}",
                valueColor = if (totals.deliveryCharge == 0.0) GreenDiscount else BrandBlack
            )

            HorizontalDivider(color = BrandBorderGrey, modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Amount",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandBlack
                )
                Text(
                    text = "₹${totals.finalTotal.toInt()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandBlack
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You will save ₹${totals.savedAmount.toInt()} on this order",
                    color = GreenDiscount,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Secure checkout placeholder button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandWhite)
                .padding(16.dp)
        ) {
            Button(
                onClick = { navController.navigate("checkout") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlack, contentColor = BrandWhite),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text("PLACE ORDER", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("100% Secure Payments | Trust badging", color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}

data class CartTotals(
    val mrpTotal: Double,
    val subtotal: Double,
    val discount: Double,
    val deliveryCharge: Double,
    val couponDiscount: Double,
    val finalTotal: Double,
    val savedAmount: Double
)

@Composable
fun CartItemCard(
    item: CartItem,
    product: Product,
    onUpdateQty: (Int) -> Unit,
    onRemove: () -> Unit,
    onMoveToWishlist: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BrandWhite),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, BrandBorderGrey)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = product.getImagesList().firstOrNull() ?: "",
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .clickable { onClick() }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
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
                        text = "Color: ${item.colorName}  |  Size: ${item.selectedSize}",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "Seller: RANGE Official",
                        color = Color.LightGray,
                        fontSize = 9.sp
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
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "₹${product.mrp.toInt()}",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${product.discountPercent}% OFF",
                            color = GreenDiscount,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BrandBorderGrey)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.border(1.dp, BrandBorderGrey, RoundedCornerShape(4.dp))
                ) {
                    IconButton(
                        onClick = { onUpdateQty(item.quantity - 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text = "${item.quantity}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { onUpdateQty(item.quantity + 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onMoveToWishlist,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("MOVE TO WISHLIST", color = BrandBlack, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Remove",
                            tint = BrandAccentRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PriceRowItem(label: String, value: String, valueColor: Color = BrandBlack) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
