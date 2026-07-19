package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.data.AppDatabase
import com.example.data.Repository
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainEcommerceScaffold()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainEcommerceScaffold() {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext
    val viewModel: MainViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.getDatabase(context)
                val repository = Repository(db)
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repository) as T
            }
        }
    )

    val cartItems: List<com.example.data.CartItem> by viewModel.cartItems.collectAsStateWithLifecycle(initialValue = emptyList())
    val wishlistItems: List<com.example.data.WishlistItem> by viewModel.wishlistItems.collectAsStateWithLifecycle(initialValue = emptyList())
    val products: List<com.example.data.Product> by viewModel.allProducts.collectAsStateWithLifecycle(initialValue = emptyList())

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Global Search states
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val searchResults = remember(searchQuery, products) {
        if (searchQuery.trim().isEmpty()) {
            emptyList<com.example.data.Product>()
        } else {
            products.filter { product ->
                product.title.contains(searchQuery, ignoreCase = true) ||
                        product.category.contains(searchQuery, ignoreCase = true) ||
                        product.subcategory.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Helper to trigger snackbar notifications easily
    val showSnackbar: (String) -> Unit = { message ->
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    // Determine bottom bar visibility
    val noBottomBarRoutes = listOf("login", "register", "success/{orderId}")
    val showBottomBar = currentRoute != null && noBottomBarRoutes.none { currentRoute.startsWith(it) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (showBottomBar) {
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = "RANGE",
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandBlack,
                                letterSpacing = 4.sp,
                                fontSize = 20.sp
                            )
                        },
                        navigationIcon = {
                            val canGoBack = navController.previousBackStackEntry != null
                            if (canGoBack && currentRoute != "home") {
                                IconButton(onClick = { navController.navigateUp() }) {
                                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandBlack)
                                }
                            } else {
                                IconButton(onClick = { showSnackbar("RANGE Premium Wear Catalog v1.0") }) {
                                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = BrandBlack)
                                }
                            }
                        },
                        actions = {
                            // Search toggle button
                            IconButton(onClick = { isSearchActive = !isSearchActive }) {
                                Icon(
                                    imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = BrandBlack
                                )
                            }

                            // Notification button
                            IconButton(onClick = { showSnackbar("No new notifications.") }) {
                                Icon(imageVector = Icons.Default.NotificationsNone, contentDescription = "Notifications", tint = BrandBlack)
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = BrandWhite
                        )
                    )

                    // Inline real-time search field
                    AnimatedVisibility(
                        visible = isSearchActive,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Surface(
                            color = BrandWhite,
                            modifier = Modifier.fillMaxWidth().border(1.dp, BrandBorderGrey)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search for Shirts, Jackets, Shoes...", fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(0.dp),
                                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                                            }
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = {
                                        keyboardController?.hide()
                                    }),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = BrandBlack,
                                        unfocusedTextColor = BrandBlack,
                                        focusedBorderColor = BrandBlack,
                                        unfocusedBorderColor = BrandBorderGrey
                                    )
                                )

                                // Real-time search suggestions panel
                                if (searchQuery.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        "SEARCH RESULTS FOR '$searchQuery' (${searchResults.size})",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (searchResults.isEmpty()) {
                                        Text(
                                            "No products matched your search. Try another query.",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(vertical = 12.dp)
                                        )
                                    } else {
                                        LazyVerticalGrid(
                                            columns = GridCells.Fixed(2),
                                            modifier = Modifier.heightIn(max = 300.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(searchResults) { product ->
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = BrandWhite),
                                                    border = BorderStroke(1.dp, BrandBorderGrey),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            isSearchActive = false
                                                            searchQuery = ""
                                                            navController.navigate("detail/${product.id}")
                                                        }
                                                ) {
                                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                        AsyncImage(
                                                            model = product.getImagesList().firstOrNull() ?: "",
                                                            contentDescription = null,
                                                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(2.dp)),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                            Text(product.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                            Text("₹${product.sellingPrice.toInt()}", fontSize = 10.sp, color = BrandAccentRed, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = BrandWhite,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(1.dp, BrandBorderGrey)
                ) {
                    val items = listOf(
                        BottomNavItem("HOME", "home", Icons.Default.Home, Icons.Outlined.Home, 0),
                        BottomNavItem("WISHLIST", "wishlist", Icons.Default.Favorite, Icons.Outlined.FavoriteBorder, wishlistItems.size),
                        BottomNavItem("BAG", "cart", Icons.Default.ShoppingBag, Icons.Outlined.ShoppingBag, cartItems.sumOf { it.quantity }),
                        BottomNavItem("ACCOUNT", "profile", Icons.Default.Person, Icons.Outlined.Person, 0)
                    )

                    items.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (item.badgeCount > 0) {
                                            Badge(containerColor = BrandAccentRed) {
                                                Text(
                                                    text = "${item.badgeCount}",
                                                    color = BrandWhite,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label,
                                        tint = if (isSelected) BrandBlack else Color.Gray,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = if (isSelected) BrandBlack else Color.Gray
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = BrandLightGrey
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(navController, viewModel, showSnackbar)
            }
            composable("register") {
                RegisterScreen(navController, viewModel, showSnackbar)
            }
            composable("home") {
                HomeScreen(navController, viewModel, showSnackbar)
            }
            composable(
                route = "category/{categoryName}",
                arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                CategoryScreen(categoryName, navController, viewModel, showSnackbar)
            }
            composable(
                route = "detail/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.IntType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getInt("productId") ?: 0
                ProductDetailScreen(productId, navController, viewModel, showSnackbar)
            }
            composable("cart") {
                CartScreen(navController, viewModel, showSnackbar)
            }
            composable("checkout") {
                CheckoutScreen(navController, viewModel, showSnackbar)
            }
            composable(
                route = "success/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderSuccessPage(orderId, navController, viewModel)
            }
            composable("wishlist") {
                WishlistScreen(navController, viewModel, showSnackbar)
            }
            composable("profile") {
                AccountScreen(navController, viewModel, showSnackbar)
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeCount: Int
)
