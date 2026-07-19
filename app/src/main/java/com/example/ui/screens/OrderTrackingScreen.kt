package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.Order
import com.example.data.OrderItem
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    navController: NavController,
    viewModel: MainViewModel,
    initialOrderId: String? = null,
    onShowSnackbar: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var orderIdInput by remember { mutableStateOf(initialOrderId ?: "") }
    var isTracking by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Active tracked order state (database order or simulated)
    var trackedOrder by remember { mutableStateOf<Order?>(null) }
    var trackedOrderItems by remember { mutableStateOf<List<OrderItem>>(emptyList()) }
    var isSimulatedOrder by remember { mutableStateOf(false) }

    // List of pre-seeded/demo order IDs for easy user interaction
    val demoOrders = remember {
        listOf(
            DemoTracking(
                id = "RANGE-2026-88492",
                status = "Delivered",
                desc = "Delivered (Yesterday)",
                badgeColor = GreenDiscount
            ),
            DemoTracking(
                id = "RANGE-2026-47392",
                status = "Out for Delivery",
                desc = "Out for Delivery (Today)",
                badgeColor = Color(0xFFFFB300)
            ),
            DemoTracking(
                id = "RANGE-2026-10294",
                status = "Shipped",
                desc = "In Transit (Near Bengaluru)",
                badgeColor = BrandDarkNavy
            ),
            DemoTracking(
                id = "RANGE-2026-99381",
                status = "Ordered",
                desc = "Ordered / Processing Hub",
                badgeColor = Color.Gray
            )
        )
    }

    // Load products from DB to enrich simulated items with real product images if possible
    val products by viewModel.allProducts.collectAsStateWithLifecycle(initialValue = emptyList())

    // Helper to perform tracking
    val performTracking: (String) -> Unit = { id ->
        val trimmedId = id.trim().uppercase()
        if (trimmedId.isEmpty()) {
            onShowSnackbar("Please enter a valid Order ID.")
        } else {
            isLoading = true
            isTracking = true
            focusManager.clearFocus()

            coroutineScope.launch {
                // Simulate network/lookup delay
                delay(1200)

                // 1. Try to find real order in DB
                var foundInDb: Order? = null
                viewModel.getOrderByStringId(trimmedId).collect { order ->
                    if (order != null) {
                        foundInDb = order
                    }
                }
                // Small buffer to guarantee collect completes
                delay(100)

                if (foundInDb != null) {
                    trackedOrder = foundInDb
                    trackedOrderItems = foundInDb!!.getOrderItems()
                    isSimulatedOrder = false
                    onShowSnackbar("Order found in history!")
                } else {
                    // 2. Fallback to generating a simulated order for tracking demo
                    val demoItem = demoOrders.find { it.id == trimmedId }
                    val orderStatus = demoItem?.status ?: "Shipped"
                    val dateSeed = when (orderStatus) {
                        "Delivered" -> System.currentTimeMillis() - 86400000L * 4
                        "Out for Delivery" -> System.currentTimeMillis() - 86400000L * 2
                        "Shipped" -> System.currentTimeMillis() - 86400000L * 1
                        else -> System.currentTimeMillis() - 3600000L * 3
                    }

                    // Create items list using real products to look extremely neat
                    val mockItems = if (products.isNotEmpty()) {
                        val p1 = products.getOrNull(0)
                        val p2 = products.getOrNull(1)
                        val list = mutableListOf<OrderItem>()
                        p1?.let {
                            list.add(
                                OrderItem(
                                    productId = it.id,
                                    title = it.title,
                                    image = it.getImagesList().firstOrNull() ?: "",
                                    color = it.getColorsList().firstOrNull()?.first ?: "Classic Blue",
                                    size = "M",
                                    quantity = 1,
                                    price = it.sellingPrice
                                )
                            )
                        }
                        if (demoItem?.id == "RANGE-2026-88492" && p2 != null) {
                            list.add(
                                OrderItem(
                                    productId = p2.id,
                                    title = p2.title,
                                    image = p2.getImagesList().firstOrNull() ?: "",
                                    color = p2.getColorsList().firstOrNull()?.first ?: "Pitch Black",
                                    size = "L",
                                    quantity = 1,
                                    price = p2.sellingPrice
                                )
                            )
                        }
                        list
                    } else {
                        listOf(
                            OrderItem(
                                productId = 1,
                                title = "Premium Slim Fit Stretch Jeans",
                                image = "https://placehold.co/400x500/1D3557/ffffff?text=Jeans",
                                color = "Dark Indigo",
                                size = "32",
                                quantity = 1,
                                price = 1299.0
                            )
                        )
                    }

                    val itemsStr = mockItems.joinToString(";") {
                        "${it.productId}|${it.title}|${it.image}|${it.color}|${it.size}|${it.quantity}|${it.price}"
                    }

                    trackedOrder = Order(
                        orderId = trimmedId,
                        orderDate = dateSeed,
                        itemsJson = itemsStr,
                        shippingAddressJson = "Sivaniranjan Swaminathan\nFlat 402, Block A, Elite Parkside\nWhitefield Main Road, Bengaluru, Karnataka - 560066\nPhone: +91 9876543210",
                        paymentMethod = "Credit Card",
                        paymentId = "pay_sim_998273",
                        paymentStatus = "Success",
                        couponAppliedCode = "RANGE100",
                        couponDiscount = 100.0,
                        subtotal = mockItems.sumOf { it.price * it.quantity } + 100.0,
                        discount = 0.0,
                        deliveryCharge = 0.0,
                        total = mockItems.sumOf { it.price * it.quantity },
                        orderStatus = orderStatus,
                        estimatedDelivery = dateSeed + 86400000L * 5
                    )
                    trackedOrderItems = mockItems
                    isSimulatedOrder = true
                    onShowSnackbar("Simulated real-time logistics connection established.")
                }
                isLoading = false
            }
        }
    }

    // Auto-trigger if initialOrderId is supplied (e.g. from Success Page or Account Order click)
    LaunchedEffect(initialOrderId) {
        if (!initialOrderId.isNullOrEmpty()) {
            orderIdInput = initialOrderId
            performTracking(initialOrderId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandLightGrey)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandWhite)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandBlack)
            }
            Text(
                text = "TRACK YOUR ORDER",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = BrandBlack,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Intro Callout Card
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, BrandBorderGrey),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REAL-TIME CARRIER SEARCH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandDarkNavy,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enter your RANGE Order ID to query the sorting facilities, customs routing, and real-time package carrier milestones.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 15.sp
                    )
                }
            }

            // Tracking Search Form Card
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, BrandBorderGrey),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ENTER RANGE ORDER ID",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = orderIdInput,
                        onValueChange = { orderIdInput = it },
                        placeholder = { Text("e.g. RANGE-2026-10294", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = BrandBlack, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (orderIdInput.isNotEmpty()) {
                                IconButton(onClick = { orderIdInput = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(onSearch = {
                            performTracking(orderIdInput)
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BrandBlack,
                            unfocusedTextColor = BrandBlack,
                            focusedBorderColor = BrandBlack,
                            unfocusedBorderColor = BrandBorderGrey
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { performTracking(orderIdInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlack),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = orderIdInput.trim().isNotEmpty() && !isLoading
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isLoading) "QUERYING NETWORK..." else "TRACK PACKAGE",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Demo / Seeded order selector
                    Text(
                        text = "OR CHOOSE A DEMO TRACKING ID TO TEST:",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    demoOrders.forEach { demo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, BrandBorderGrey, RoundedCornerShape(2.dp))
                                .clickable {
                                    orderIdInput = demo.id
                                    performTracking(demo.id)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(demo.id, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = BrandBlack)
                                Text(demo.desc, fontSize = 9.sp, color = Color.Gray)
                            }
                            Box(
                                modifier = Modifier
                                    .background(demo.badgeColor.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    demo.status.uppercase(),
                                    color = demo.badgeColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }
            }

            // Real-Time Results Section
            AnimatedVisibility(
                visible = isTracking,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                if (isLoading) {
                    // Loading State
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BrandWhite),
                        border = BorderStroke(1.dp, BrandBorderGrey),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = BrandBlack, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "QUERYING RANGE LOGISTICS NETWORK...",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDarkNavy,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "Scanning hub registers and delivery checkpoints...",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    trackedOrder?.let { order ->
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Status Summary Banner Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BrandDarkNavy),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("ORDER ID: #${order.orderId}", color = BrandWhite.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = order.orderStatus.uppercase(),
                                                color = BrandWhite,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 1.sp
                                            )
                                        }

                                        val statusIcon = when (order.orderStatus) {
                                            "Ordered" -> Icons.Default.Inventory2
                                            "Shipped" -> Icons.Default.LocalShipping
                                            "Out for Delivery" -> Icons.Default.DirectionsBike
                                            "Delivered" -> Icons.Default.CheckCircle
                                            else -> Icons.Default.Cancel
                                        }

                                        Icon(
                                            imageVector = statusIcon,
                                            contentDescription = null,
                                            tint = BrandWhite,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = BrandWhite.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("ESTIMATED ARRIVAL", color = BrandWhite.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            val estDateStr = remember(order.estimatedDelivery) {
                                                val sdf = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
                                                sdf.format(Date(order.estimatedDelivery))
                                            }
                                            Text(estDateStr, color = BrandWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("CARRIER", color = BrandWhite.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Text("RANGE EXPRESS LOGS", color = BrandWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Interactive Live Movement Simulator Panel
                            if (isSimulatedOrder && order.orderStatus != "Cancelled") {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)), // Highlighted yellow background
                                    border = BorderStroke(1.dp, Color(0xFFFBC02D)),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "LIVE LOGISTICS SIMULATOR ACTIVE",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 10.sp,
                                                color = Color(0xFFE65100),
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "This demo is connected to our simulated carrier network. You can advance the delivery truck milestone manually to view real-time changes!",
                                            fontSize = 9.5.sp,
                                            color = Color.DarkGray,
                                            lineHeight = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (order.orderStatus != "Delivered") {
                                                Button(
                                                    onClick = {
                                                        val nextStatus = when (order.orderStatus) {
                                                            "Ordered" -> "Shipped"
                                                            "Shipped" -> "Out for Delivery"
                                                            "Out for Delivery" -> "Delivered"
                                                            else -> "Delivered"
                                                        }
                                                        trackedOrder = order.copy(orderStatus = nextStatus)
                                                        onShowSnackbar("Milestone updated: Order status is now '$nextStatus'!")
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlack),
                                                    shape = RoundedCornerShape(0.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("ADVANCE MILESTONE 🚚", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }

                                            Button(
                                                onClick = {
                                                    trackedOrder = order.copy(orderStatus = "Ordered")
                                                    onShowSnackbar("Reset back to Order Placed.")
                                                },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlack),
                                                border = BorderStroke(1.dp, BrandBlack),
                                                shape = RoundedCornerShape(0.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.weight(0.7f)
                                            ) {
                                                Text("RESET ROUTE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            // Package Progress Milestones (Vertical Timeline)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                                border = BorderStroke(1.dp, BrandBorderGrey),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "DELIVERY JOURNEY LOGS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BrandBlack,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Build Milestones based on current status
                                    val milestones = remember(order.orderStatus, order.orderDate) {
                                        getMilestonesList(order.orderStatus, order.orderDate)
                                    }

                                    milestones.forEachIndexed { index, m ->
                                        VerticalTimelineItem(
                                            milestone = m,
                                            isLast = index == milestones.size - 1
                                        )
                                    }
                                }
                            }

                            // Items in this delivery
                            if (trackedOrderItems.isNotEmpty()) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = BrandWhite),
                                    border = BorderStroke(1.dp, BrandBorderGrey),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            "ITEMS IN THIS SHIPMENT (${trackedOrderItems.size})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BrandBlack,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        trackedOrderItems.forEach { item ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 6.dp)
                                            ) {
                                                AsyncImage(
                                                    model = item.image,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(45.dp)
                                                        .clip(RoundedCornerShape(2.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(item.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandBlack, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Text("Size: ${item.size}  |  Color: ${item.color}  |  Qty: ${item.quantity}", fontSize = 9.sp, color = Color.Gray)
                                                }
                                                Text("₹${(item.price * item.quantity).toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
                                            }
                                            HorizontalDivider(color = BrandBorderGrey, modifier = Modifier.padding(vertical = 4.dp))
                                        }
                                    }
                                }
                            }

                            // Shipping Destination
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                                border = BorderStroke(1.dp, BrandBorderGrey),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = BrandAccentRed, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "SHIPPING DESTINATION",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BrandBlack,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = order.shippingAddressJson,
                                        fontSize = 11.sp,
                                        color = Color.DarkGray,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(start = 22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VerticalTimelineItem(
    milestone: TrackingMilestone,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Vertical indicator column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (milestone.isCompleted) GreenDiscount else Color.LightGray)
                    .border(2.dp, BrandWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (milestone.isCompleted && !milestone.isCurrent) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = BrandWhite, modifier = Modifier.size(8.dp))
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(55.dp)
                        .background(if (milestone.isCompleted) GreenDiscount else Color.LightGray)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Milestones Description text
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = milestone.title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = if (milestone.isCurrent) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (milestone.isCurrent) BrandDarkNavy else if (milestone.isCompleted) BrandBlack else Color.Gray
                )

                if (milestone.date.isNotEmpty()) {
                    Text(
                        text = milestone.date,
                        fontSize = 9.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = milestone.description,
                fontSize = 10.sp,
                color = if (milestone.isCompleted) Color.DarkGray else Color.Gray,
                lineHeight = 13.sp
            )

            if (milestone.location.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = milestone.location,
                        fontSize = 8.5.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Model for demo listings
data class DemoTracking(
    val id: String,
    val status: String,
    val desc: String,
    val badgeColor: Color
)

// Model for milestone items
data class TrackingMilestone(
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean
)

// Helper to assemble logical milestones list
fun getMilestonesList(status: String, orderDate: Long): List<TrackingMilestone> {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    val orderedDateStr = sdf.format(Date(orderDate))
    val shippedDateStr = sdf.format(Date(orderDate + 86400000L * 1))
    val outDateStr = sdf.format(Date(orderDate + 86400000L * 3))
    val deliveredDateStr = sdf.format(Date(orderDate + 86400000L * 4))

    return listOf(
        TrackingMilestone(
            title = "Order Confirmed & Placed",
            description = "Your premium attire order has been locked in and passed on to our warehouse sorting crew.",
            date = orderedDateStr,
            location = "RANGE Sorting Center, Mumbai",
            isCompleted = true,
            isCurrent = status == "Ordered"
        ),
        TrackingMilestone(
            title = "Shipped & Dispatched",
            description = "The shipping parcel has been sealed, barcode stamped, and loaded onto an express transport truck.",
            date = if (status != "Ordered") shippedDateStr else "",
            location = "In-transit Hub, Pune sorting yard",
            isCompleted = status != "Ordered",
            isCurrent = status == "Shipped"
        ),
        TrackingMilestone(
            title = "Out for Delivery",
            description = "Your RANGE package has arrived locally and is out for final delivery with executive Rahul Kumar (+91 9123456780).",
            date = if (status == "Out for Delivery" || status == "Delivered") outDateStr else "",
            location = "Local Delivery Depot, Bengaluru",
            isCompleted = status == "Out for Delivery" || status == "Delivered",
            isCurrent = status == "Out for Delivery"
        ),
        TrackingMilestone(
            title = "Successfully Delivered",
            description = "Handed directly to resident. Thank you for making RANGE your preferred fashion brand!",
            date = if (status == "Delivered") deliveredDateStr else "",
            location = "Shipping Destination, Bengaluru",
            isCompleted = status == "Delivered",
            isCurrent = status == "Delivered"
        )
    )
}
