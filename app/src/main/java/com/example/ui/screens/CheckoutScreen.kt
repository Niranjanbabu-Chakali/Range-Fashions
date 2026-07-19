package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.CartItem
import com.example.data.Product
import com.example.data.UserAddress
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlin.random.Random

@Composable
fun CheckoutScreen(
    navController: NavController,
    viewModel: MainViewModel,
    onShowSnackbar: (String) -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()
    val selectedAddress by viewModel.selectedAddress.collectAsStateWithLifecycle()
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()

    var checkoutStep by remember { mutableIntStateOf(1) } // 1: Address, 2: Summary, 3: Payment
    var showAddressForm by remember { mutableStateOf(false) }

    // State variables for checkout payment simulator
    var selectedPaymentMethod by remember { mutableStateOf("Razorpay UPI") }
    var processingPayment by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Calculations (same logic as cart)
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

    if (cartItems.isEmpty() && !processingPayment) {
        // Redundant check in case cart got cleared
        LaunchedEffect(Unit) {
            navController.navigate("home")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandLightGrey)
            .verticalScroll(scrollState)
    ) {
        // Checkout Header Stepper
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandWhite)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepIndicator(1, "Address", checkoutStep >= 1)
            Text("—", color = Color.LightGray)
            StepIndicator(2, "Summary", checkoutStep >= 2)
            Text("—", color = Color.LightGray)
            StepIndicator(3, "Payment", checkoutStep >= 3)
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (checkoutStep) {
            1 -> {
                // STEP 1: Address selection & creation
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        "SELECT DELIVERY ADDRESS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlack,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (addresses.isEmpty()) {
                        Text(
                            "No saved addresses found. Please add a delivery address.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    } else {
                        addresses.forEach { addr ->
                            val isSelected = selectedAddress?.id == addr.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) BrandBlack else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable { viewModel.selectAddress(addr) },
                                colors = CardDefaults.cardColors(containerColor = BrandWhite)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.selectAddress(addr) },
                                        colors = RadioButtonDefaults.colors(selectedColor = BrandBlack)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = addr.fullName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = BrandBlack
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(Color.LightGray)
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(addr.addressType, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${addr.addressLine1}, ${addr.addressLine2}\n" +
                                                    "${addr.city}, ${addr.state} - ${addr.pincode}\n" +
                                                    "Phone: ${addr.phone}",
                                            fontSize = 11.sp,
                                            color = Color.DarkGray,
                                            lineHeight = 14.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteAddress(addr.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showAddressForm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlack),
                        border = BorderStroke(1.dp, BrandBlack),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ADD NEW ADDRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Next step button
                    Button(
                        onClick = {
                            if (selectedAddress == null) {
                                onShowSnackbar("Please select a delivery address.")
                            } else {
                                checkoutStep = 2
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlack),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("DELIVER TO THIS ADDRESS", fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                    }
                }
            }

            2 -> {
                // STEP 2: Order summary check
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        "BAG SUMMARY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlack,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Address summary display
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BrandWhite),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Delivery Address", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                TextButton(onClick = { checkoutStep = 1 }) {
                                    Text("CHANGE", color = BrandAccentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            selectedAddress?.let { addr ->
                                Text(
                                    text = "${addr.fullName}\n" +
                                            "${addr.addressLine1}, ${addr.addressLine2}\n" +
                                            "${addr.city}, ${addr.state} - ${addr.pincode}",
                                    fontSize = 11.sp,
                                    color = BrandBlack,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Summarized items cards
                    cartItems.forEach { item ->
                        val p = products.find { it.id == item.productId }
                        if (p != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(modifier = Modifier.padding(8.dp)) {
                                    AsyncImage(
                                        model = p.getImagesList().firstOrNull() ?: "",
                                        contentDescription = p.title,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(p.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandBlack, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("Size: ${item.selectedSize}  |  Qty: ${item.quantity}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Text("₹${(p.sellingPrice * item.quantity).toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { checkoutStep = 3 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlack),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("PROCEED TO PAYMENT", fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                    }
                }
            }

            3 -> {
                // STEP 3: Payment details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        "SELECT PAYMENT METHOD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlack,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Gateway Options
                    val gateways = listOf(
                        "Razorpay UPI (GPay/PhonePe)",
                        "Razorpay Credit / Debit Card",
                        "Razorpay Net Banking",
                        "Cash on Delivery (COD) - Extra ₹49"
                    )

                    gateways.forEach { gateway ->
                        val isSelected = selectedPaymentMethod == gateway
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) BrandBlack else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { selectedPaymentMethod = gateway },
                            colors = CardDefaults.cardColors(containerColor = BrandWhite)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedPaymentMethod = gateway },
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandBlack)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(gateway, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandBlack)
                                    if (gateway.startsWith("Razorpay")) {
                                        Text("Secure checkout via Razorpay testing sandbox", fontSize = 10.sp, color = Color.Gray)
                                    } else {
                                        Text("Pay cash on actual courier delivery", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Final price billing summary card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BrandWhite),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("PAYMENT BILLING BREAKDOWN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Adjust total for COD extra charges if selected
                            val finalBillAmount = if (selectedPaymentMethod.contains("COD")) totals.finalTotal + 49.0 else totals.finalTotal

                            PriceRowItem("Bag Total", "₹${totals.subtotal.toInt()}")
                            if (selectedPaymentMethod.contains("COD")) {
                                PriceRowItem("COD Processing Fee", "₹49")
                            }
                            PriceRowItem("Delivery Charges", if (totals.deliveryCharge == 0.0) "FREE" else "₹${totals.deliveryCharge.toInt()}")

                            HorizontalDivider(color = BrandBorderGrey, modifier = Modifier.padding(vertical = 12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Amount Payable", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("₹${finalBillAmount.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (processingPayment) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = BrandBlack)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Simulating Razorpay Payment Sandbox...", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        val finalBillAmount = if (selectedPaymentMethod.contains("COD")) totals.finalTotal + 49.0 else totals.finalTotal
                        Button(
                            onClick = {
                                processingPayment = true
                                // Simulate Razorpay Gateway delay of 2.5 seconds
                                viewModel.placeOrder(
                                    paymentMethod = selectedPaymentMethod,
                                    subtotal = totals.subtotal,
                                    discount = totals.discount,
                                    deliveryCharge = totals.deliveryCharge + (if (selectedPaymentMethod.contains("COD")) 49.0 else 0.0),
                                    couponDiscount = totals.couponDiscount,
                                    total = finalBillAmount,
                                    cartItemsList = cartItems,
                                    productsList = products,
                                    onSuccess = { generatedId ->
                                        processingPayment = false
                                        navController.navigate("success/$generatedId")
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlack),
                            shape = RoundedCornerShape(0.dp)
                        ) {
                            Text("PAY ₹${finalBillAmount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }

    // New Address addition form modal dialog
    if (showAddressForm) {
        Dialog(onDismissRequest = { showAddressForm = false }) {
            var name by remember { mutableStateOf("") }
            var phone by remember { mutableStateOf("") }
            var line1 by remember { mutableStateOf("") }
            var line2 by remember { mutableStateOf("") }
            var city by remember { mutableStateOf("") }
            var state by remember { mutableStateOf("") }
            var pincodeInput by remember { mutableStateOf("") }
            var isHomeType by remember { mutableStateOf(true) }

            Card(
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Add Shipping Address", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
                        IconButton(onClick = { showAddressForm = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AddressField("Full Name", name) { name = it }
                    AddressField("Phone Number", phone, KeyboardType.Phone) { phone = it }
                    AddressField("Flat / House No. / Area", line1) { line1 = it }
                    AddressField("Street / Landmark", line2) { line2 = it }
                    AddressField("Pin Code (6 digits)", pincodeInput, KeyboardType.Number) { input ->
                        pincodeInput = input
                        // Simulated automatic lookup
                        if (input.length == 6) {
                            city = "Bengaluru"
                            state = "Karnataka"
                        }
                    }
                    AddressField("City / Town", city) { city = it }
                    AddressField("State", state) { state = it }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Address Type", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = isHomeType,
                            onClick = { isHomeType = true },
                            label = { Text("Home") },
                            shape = RoundedCornerShape(2.dp)
                        )
                        FilterChip(
                            selected = !isHomeType,
                            onClick = { isHomeType = false },
                            label = { Text("Office") },
                            shape = RoundedCornerShape(2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (name.isNotEmpty() && phone.isNotEmpty() && line1.isNotEmpty() && city.isNotEmpty() && pincodeInput.length == 6) {
                                val addr = UserAddress(
                                    fullName = name,
                                    phone = phone,
                                    addressLine1 = line1,
                                    addressLine2 = line2,
                                    city = city,
                                    state = state,
                                    pincode = pincodeInput,
                                    landmark = line2,
                                    addressType = if (isHomeType) "Home" else "Office",
                                    isDefault = addresses.isEmpty() // Set default if first address
                                )
                                viewModel.saveAddress(addr)
                                showAddressForm = false
                                onShowSnackbar("Shipping address saved!")
                            } else {
                                onShowSnackbar("Please fill all required shipping fields.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlack),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SAVE ADDRESS")
                    }
                }
            }
        }
    }
}

@Composable
fun AddressField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            singleLine = true,
            shape = RoundedCornerShape(0.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BrandBlack,
                unfocusedTextColor = BrandBlack,
                focusedBorderColor = BrandBlack,
                unfocusedBorderColor = BrandBorderGrey
            )
        )
    }
}

@Composable
fun StepIndicator(step: Int, title: String, isActive: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isActive) BrandBlack else Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("$step", color = BrandWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            title,
            color = if (isActive) BrandBlack else Color.Gray,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ORDER SUCCESS PAGE
@Composable
fun OrderSuccessPage(
    orderId: String,
    navController: NavController,
    viewModel: MainViewModel
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val order = orders.find { it.orderId == orderId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandWhite)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Confetti effect simulated by glowing icons
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFFE8F5E9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = "Success",
                tint = GreenDiscount,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ORDER PLACED SUCCESSFULLY!",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BrandBlack,
            letterSpacing = 1.5.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Thank you for shopping with RANGE. Your premium wardrobe addition is locked in!",
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Receipt Card details
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandLightGrey),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Order Number", fontSize = 11.sp, color = Color.Gray)
                    Text(orderId, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Payment Method", fontSize = 11.sp, color = Color.Gray)
                    Text(order?.paymentMethod ?: "Secure Card", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Est. Delivery", fontSize = 11.sp, color = Color.Gray)
                    Text("Dec 28, Saturday", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenDiscount)
                }

                order?.let { ord ->
                    HorizontalDivider(color = BrandBorderGrey, modifier = Modifier.padding(vertical = 12.dp))
                    Text("Billing Details", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = ord.shippingAddressJson,
                        fontSize = 11.sp,
                        color = BrandBlack,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // CTAs
        Button(
            onClick = { navController.navigate("home") },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlack),
            shape = RoundedCornerShape(0.dp)
        ) {
            Text("CONTINUE SHOPPING", fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { navController.navigate("profile") },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlack),
            border = BorderStroke(1.dp, BrandBlack),
            shape = RoundedCornerShape(0.dp)
        ) {
            Text("VIEW ORDER DETAILS", fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
        }
    }
}
