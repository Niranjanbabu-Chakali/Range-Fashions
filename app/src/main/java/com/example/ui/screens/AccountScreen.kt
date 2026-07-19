package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.Order
import com.example.data.OrderItem
import com.example.data.UserAddress
import com.example.data.UserProfile
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun AccountScreen(
    navController: NavController,
    viewModel: MainViewModel,
    onShowSnackbar: (String) -> Unit
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("Profile") } // "Profile", "Orders", "Addresses", "Support"

    // Sub-states
    var selectedOrderDetailId by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandLightGrey)
    ) {
        // User Header
        profile?.let { prof ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandDarkNavy)
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar placeholder with initials
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(BrandWhite, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = prof.fullName.take(2).uppercase(),
                        color = BrandBlack,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = prof.fullName.uppercase(),
                        color = BrandWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = prof.email,
                        color = BrandWhite.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Horizontal Tabs Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandWhite)
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val tabs = listOf("Profile", "My Orders", "Addresses", "Support")
            tabs.forEach { tab ->
                val isSelected = (tab == activeTab && selectedOrderDetailId == null) || (tab == "My Orders" && selectedOrderDetailId != null)
                TextButton(
                    onClick = {
                        activeTab = tab
                        selectedOrderDetailId = null
                    }
                ) {
                    Text(
                        text = tab.uppercase(),
                        color = if (isSelected) BrandBlack else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Screens Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            if (selectedOrderDetailId != null) {
                val ord = orders.find { it.orderId == selectedOrderDetailId }
                if (ord != null) {
                    OrderDetailPanel(
                        order = ord,
                        onBack = { selectedOrderDetailId = null },
                        onCancel = {
                            viewModel.cancelOrder(ord.id)
                            onShowSnackbar("Order cancellation request sent.")
                        }
                    )
                }
            } else {
                when (activeTab) {
                    "Profile" -> ProfilePanel(profile, viewModel, onShowSnackbar)
                    "My Orders" -> OrdersPanel(orders, onSelectOrder = { selectedOrderDetailId = it })
                    "Addresses" -> AddressesPanel(addresses, viewModel, onShowSnackbar)
                    "Support" -> SupportPanel(onShowSnackbar)
                }
            }
        }
    }
}

// 1. PROFILE SUB-PANEL
@Composable
fun ProfilePanel(
    profile: UserProfile?,
    viewModel: MainViewModel,
    onShowSnackbar: (String) -> Unit
) {
    if (profile == null) return

    var name by remember { mutableStateOf(profile.fullName) }
    var email by remember { mutableStateOf(profile.email) }
    var phone by remember { mutableStateOf(profile.phone) }
    var gender by remember { mutableStateOf(profile.gender) }
    var dob by remember { mutableStateOf(profile.dob) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(BrandWhite)
            .padding(16.dp)
    ) {
        Text("EDIT PROFILE INFORMATION", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
        Spacer(modifier = Modifier.height(12.dp))

        AddressField("Full Name", name) { name = it }
        AddressField("Email ID", email, KeyboardType.Email) { email = it }
        AddressField("Phone Number", phone, KeyboardType.Phone) { phone = it }
        AddressField("Gender", gender) { gender = it }
        AddressField("Date of Birth (DD/MM/YYYY)", dob) { dob = it }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (name.isNotEmpty() && email.isNotEmpty()) {
                    val updated = UserProfile(
                        fullName = name,
                        email = email,
                        phone = phone,
                        gender = gender,
                        dob = dob,
                        profilePicUrl = profile.profilePicUrl
                    )
                    viewModel.updateProfile(updated)
                    onShowSnackbar("Profile details updated successfully!")
                } else {
                    onShowSnackbar("Name and Email are required.")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlack),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SAVE CHANGES", fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

// 2. ORDERS SUB-PANEL
@Composable
fun OrdersPanel(
    orders: List<Order>,
    onSelectOrder: (String) -> Unit
) {
    if (orders.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(60.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("No orders placed yet", fontWeight = FontWeight.Bold, color = BrandBlack, fontSize = 14.sp)
            Text("Your purchase history will appear here.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        orders.forEach { order ->
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, BrandBorderGrey),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectOrder(order.orderId) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ID: #${order.orderId}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandBlack)
                            val dateStr = remember(order.orderDate) {
                                val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                                sdf.format(java.util.Date(order.orderDate))
                            }
                            Text("Date: $dateStr", color = Color.Gray, fontSize = 10.sp)
                        }

                        // Order status box
                        val statusColor = when (order.orderStatus) {
                            "Ordered" -> BrandDarkNavy
                            "Delivered" -> GreenDiscount
                            "Cancelled" -> BrandAccentRed
                            else -> Color(0xFFFFB300)
                        }
                        Box(
                            modifier = Modifier
                                .background(statusColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(order.orderStatus, color = BrandWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Items snippet
                    val items = order.getOrderItems()
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = items.firstOrNull()?.image ?: "",
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (items.size > 1) "${items.firstOrNull()?.title} & ${items.size - 1} more items" else items.firstOrNull()?.title ?: "",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text("₹${order.total.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Visual Progress Timeline
                    if (order.orderStatus != "Cancelled") {
                        TimelineProgress(order.orderStatus)
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineProgress(status: String) {
    val steps = listOf("Ordered", "Shipped", "Delivered")
    val currentIndex = when (status) {
        "Shipped" -> 1
        "Delivered" -> 2
        else -> 0
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { i, step ->
            val isPassed = i <= currentIndex
            val labelColor = if (isPassed) GreenDiscount else Color.LightGray

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isPassed) GreenDiscount else Color.LightGray)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(step, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = labelColor)
            }
            if (i < steps.size - 1) {
                HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = if (i < currentIndex) GreenDiscount else Color.LightGray)
            }
        }
    }
}

// ORDER DETAIL SUB-PANEL (RECEIPT VIEW)
@Composable
fun OrderDetailPanel(
    order: Order,
    onBack: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(BrandWhite)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("ORDER DETAILS", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(32.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Order Number: #${order.orderId}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
        Spacer(modifier = Modifier.height(6.dp))

        // Visual Status
        TimelineProgress(order.orderStatus)

        Spacer(modifier = Modifier.height(16.dp))

        Text("SHIPPING DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(order.shippingAddressJson, fontSize = 11.sp, color = BrandBlack, lineHeight = 14.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text("ITEMS PURCHASED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        order.getOrderItems().forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                AsyncImage(
                    model = item.image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Qty: ${item.quantity} | Size: ${item.size} | Color: ${item.color}", fontSize = 10.sp, color = Color.Gray)
                }
                Text("₹${(item.price * item.quantity).toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("BILLING SUMMARY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(6.dp))

        PriceRowItem("Subtotal", "₹${order.subtotal.toInt()}")
        if (order.couponAppliedCode.isNotEmpty()) {
            PriceRowItem("Coupon (${order.couponAppliedCode})", "-₹${order.couponDiscount.toInt()}", valueColor = GreenDiscount)
        }
        PriceRowItem("Delivery Charges", if (order.deliveryCharge == 0.0) "FREE" else "₹${order.deliveryCharge.toInt()}")
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        PriceRowItem("Total Paid", "₹${order.total.toInt()}")

        Spacer(modifier = Modifier.height(24.dp))

        // Cancel order button
        if (order.orderStatus == "Ordered") {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = BrandAccentRed),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("CANCEL ORDER", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

// 3. ADDRESSES SUB-PANEL
@Composable
fun AddressesPanel(
    addresses: List<UserAddress>,
    viewModel: MainViewModel,
    onShowSnackbar: (String) -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("SAVED ADDRESSES", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
            Button(
                onClick = { showAddForm = true },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlack),
                shape = RoundedCornerShape(0.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ADD", fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        addresses.forEach { addr ->
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, BrandBorderGrey),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(addr.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandBlack)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.background(Color.LightGray).padding(2.dp)) {
                                Text(addr.addressType, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                            if (addr.isDefault) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("[Default]", color = GreenDiscount, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row {
                            if (!addr.isDefault) {
                                TextButton(onClick = { viewModel.setAddressAsDefault(addr.id) }) {
                                    Text("SET DEFAULT", color = BrandDarkNavy, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            IconButton(onClick = { viewModel.deleteAddress(addr.id) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Text(
                        text = "${addr.addressLine1}, ${addr.addressLine2}\n" +
                                "${addr.city}, ${addr.state} - ${addr.pincode}\n" +
                                "Phone: ${addr.phone}",
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }

    if (showAddForm) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var line1 by remember { mutableStateOf("") }
        var line2 by remember { mutableStateOf("") }
        var city by remember { mutableStateOf("") }
        var state by remember { mutableStateOf("") }
        var pincodeInput by remember { mutableStateOf("") }
        var isHomeType by remember { mutableStateOf(true) }

        Dialog(onDismissRequest = { showAddForm = false }) {
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
                        IconButton(onClick = { showAddForm = false }) {
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
                                    isDefault = addresses.isEmpty()
                                )
                                viewModel.saveAddress(addr)
                                showAddForm = false
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

// 4. CUSTOMER SUPPORT SUB-PANEL (Policies, Accordion, Support Forms)
@Composable
fun SupportPanel(onShowSnackbar: (String) -> Unit) {
    var queryName by remember { mutableStateOf("") }
    var queryEmail by remember { mutableStateOf("") }
    var queryMsg by remember { mutableStateOf("") }

    var selectedSection by remember { mutableStateOf("") } // "About", "FAQ", "Policies"

    Column(modifier = Modifier.padding(16.dp)) {
        if (selectedSection == "About") {
            AboutPanel(onBack = { selectedSection = "" })
        } else if (selectedSection == "FAQ") {
            FAQPanel(onBack = { selectedSection = "" })
        } else if (selectedSection == "Policies") {
            PoliciesPanel(onBack = { selectedSection = "" })
        } else {
            // Main options list
            Text("CUSTOMER HELP CENTER", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
            Spacer(modifier = Modifier.height(12.dp))

            SupportMenuRow("About RANGE brand story", Icons.Default.History) { selectedSection = "About" }
            SupportMenuRow("Frequently Asked Questions", Icons.Default.Quiz) { selectedSection = "FAQ" }
            SupportMenuRow("Shipping & Return policies", Icons.Default.LocalShipping) { selectedSection = "Policies" }

            Spacer(modifier = Modifier.height(24.dp))

            // Support Form
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, BrandBorderGrey),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("SEND AN INQUIRY", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = BrandBlack, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    AddressField("Your Name", queryName) { queryName = it }
                    AddressField("Email ID", queryEmail, KeyboardType.Email) { queryEmail = it }
                    AddressField("Inquiry / Message", queryMsg) { queryMsg = it }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (queryName.isNotEmpty() && queryEmail.isNotEmpty() && queryMsg.isNotEmpty()) {
                                onShowSnackbar("Inquiry sent successfully! We will email you within 24 hours.")
                                queryName = ""
                                queryEmail = ""
                                queryMsg = ""
                            } else {
                                onShowSnackbar("Please fill all form fields.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlack),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SEND MESSAGE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SupportMenuRow(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BrandWhite),
        border = BorderStroke(1.dp, BrandBorderGrey),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = BrandBlack, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun AboutPanel(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandWhite)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null) }
            Text("BRAND STORY", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Welcome to RANGE.\n\n" +
                    "Founded with a vision to deliver premium, minimal, and high-quality clothing and footwear, RANGE represents sophistication and modern elegance.\n\n" +
                    "We design carefully curated apparel lines for Men and Kids, alongside hand-finished Footwear that maximizes both comfort and luxury.\n\n" +
                    "We prioritize organic fibers, zero-waste packaging, and secure payment infrastructures to give you the dapper lifestyle addition you deserve.\n\n" +
                    "Wear the Range.",
            fontSize = 12.sp,
            color = Color.DarkGray,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun FAQPanel(onBack: () -> Unit) {
    var q1 by remember { mutableStateOf(false) }
    var q2 by remember { mutableStateOf(false) }
    var q3 by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandWhite)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null) }
            Text("FAQs", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))

        DropdownRow("How do I track my order?", q1, { q1 = !q1 }) {
            Text("Go to My Account -> My Orders to see your package live tracking timeline (Shipped, Out for Delivery, Delivered).", fontSize = 11.sp, color = Color.Gray)
        }
        HorizontalDivider()
        DropdownRow("What is your return policy?", q2, { q2 = !q2 }) {
            Text("We offer easy 7-day returns or size exchanges. Place a return request inside your order details block.", fontSize = 11.sp, color = Color.Gray)
        }
        HorizontalDivider()
        DropdownRow("Do you offer Cash on Delivery?", q3, { q3 = !q3 }) {
            Text("Yes, we offer Cash on Delivery across all PIN codes in India. A nominal processing fee of ₹49 applies.", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun PoliciesPanel(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandWhite)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null) }
            Text("POLICIES", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "SHIPPING POLICY:\n" +
                    "• All orders above ₹999 qualify for Free Shipping.\n" +
                    "• Orders are processed within 24 hours and delivered in 3-5 business days.\n\n" +
                    "RETURNS & REFUNDS POLICY:\n" +
                    "• Returns can be initiated within 7 days of package delivery.\n" +
                    "• Returned items must be unwashed, unworn, and have original brand tags attached.\n" +
                    "• Refunds are credited directly to your bank account or Razorpay wallet within 48 hours of return package verification.",
            fontSize = 11.sp,
            color = Color.DarkGray,
            lineHeight = 16.sp
        )
    }
}
