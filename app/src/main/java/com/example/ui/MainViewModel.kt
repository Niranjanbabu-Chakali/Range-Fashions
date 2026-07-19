package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainViewModel(private val repository: Repository) : ViewModel() {

    // Data streams from repository
    val allProducts: StateFlow<List<Product>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItem>> = repository.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistItems: StateFlow<List<WishlistItem>> = repository.getWishlistItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val addresses: StateFlow<List<UserAddress>> = repository.getAddresses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<Order>> = repository.getOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val coupons: StateFlow<List<Coupon>> = repository.getCoupons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<UserProfile?> = repository.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Local UI states
    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon.asStateFlow()

    private val _selectedAddress = MutableStateFlow<UserAddress?>(null)
    val selectedAddress: StateFlow<UserAddress?> = _selectedAddress.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter States for Category Screen
    private val _selectedSubcategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedSubcategories: StateFlow<Set<String>> = _selectedSubcategories.asStateFlow()

    private val _priceRange = MutableStateFlow(0f..10000f)
    val priceRange: StateFlow<ClosedFloatingPointRange<Float>> = _priceRange.asStateFlow()

    private val _selectedSizes = MutableStateFlow<Set<String>>(emptySet())
    val selectedSizes: StateFlow<Set<String>> = _selectedSizes.asStateFlow()

    private val _selectedColors = MutableStateFlow<Set<String>>(emptySet())
    val selectedColors: StateFlow<Set<String>> = _selectedColors.asStateFlow()

    private val _selectedDiscount = MutableStateFlow<Int?>(null) // e.g. 10, 20, 30, 50
    val selectedDiscount: StateFlow<Int?> = _selectedDiscount.asStateFlow()

    private val _selectedRating = MutableStateFlow<Float?>(null) // e.g. 4.0, 3.0
    val selectedRating: StateFlow<Float?> = _selectedRating.asStateFlow()

    private val _sortType = MutableStateFlow("Relevance") // "PriceLowToHigh", "PriceHighToLow", "Newest", "Rating"
    val sortType: StateFlow<String> = _sortType.asStateFlow()

    // Recently Viewed Products Track
    private val _recentlyViewedIds = MutableStateFlow<List<Int>>(emptyList())
    val recentlyViewedIds: StateFlow<List<Int>> = _recentlyViewedIds.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            // Set initial address selection once addresses load
            addresses.collect { list ->
                if (_selectedAddress.value == null && list.isNotEmpty()) {
                    _selectedAddress.value = list.find { it.isDefault } ?: list.first()
                }
            }
        }
    }

    // Cart Operations
    fun addToCart(productId: Int, color: String, size: String, quantity: Int) {
        viewModelScope.launch {
            repository.addToCart(productId, color, size, quantity)
        }
    }

    fun updateCartQty(cartItemId: Int, qty: Int) {
        viewModelScope.launch {
            repository.updateCartQty(cartItemId, qty)
        }
    }

    fun removeFromCart(cartItemId: Int) {
        viewModelScope.launch {
            repository.removeFromCart(cartItemId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // Wishlist Operations
    fun toggleWishlist(productId: Int) {
        viewModelScope.launch {
            repository.toggleWishlist(productId)
        }
    }

    fun isWishlisted(productId: Int): Flow<Boolean> = repository.isWishlisted(productId)

    // Address Operations
    fun saveAddress(address: UserAddress) {
        viewModelScope.launch {
            repository.addAddress(address)
        }
    }

    fun deleteAddress(id: Int) {
        viewModelScope.launch {
            repository.deleteAddress(id)
            if (_selectedAddress.value?.id == id) {
                _selectedAddress.value = null
            }
        }
    }

    fun selectAddress(address: UserAddress) {
        _selectedAddress.value = address
    }

    fun setAddressAsDefault(id: Int) {
        viewModelScope.launch {
            repository.setAddressAsDefault(id)
        }
    }

    // Profile Operations
    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    // Coupon Operations
    fun applyCoupon(code: String, orderAmount: Double): String? {
        // Returns error message if invalid, null if successful
        var resultMessage: String? = "Coupon not found."
        viewModelScope.launch {
            val coupon = repository.getCouponByCode(code.trim().uppercase())
            if (coupon != null) {
                if (orderAmount >= coupon.minOrderAmount) {
                    _appliedCoupon.value = coupon
                    resultMessage = null
                } else {
                    resultMessage = "Minimum order amount of ₹${coupon.minOrderAmount.toInt()} required."
                }
            }
        }
        // Small delay to simulate fetch/lookup
        Thread.sleep(100) 
        return if (_appliedCoupon.value?.code == code.trim().uppercase()) null else resultMessage
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
    }

    // Order Placement
    fun placeOrder(
        paymentMethod: String,
        paymentId: String = "pay_sim_${Random.nextInt(100000, 999999)}",
        paymentStatus: String = "Success",
        subtotal: Double,
        discount: Double,
        deliveryCharge: Double,
        couponDiscount: Double,
        total: Double,
        cartItemsList: List<CartItem>,
        productsList: List<Product>,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val currentAddress = _selectedAddress.value ?: return@launch
            val orderNum = Random.nextInt(10000, 99999)
            val generatedOrderId = "RANGE-2026-$orderNum"

            // Construct itemsJson string
            // "productId|title|image|color|size|quantity|price" delimited by ";"
            val itemsString = cartItemsList.mapNotNull { cartItem ->
                val p = productsList.find { it.id == cartItem.productId }
                if (p != null) {
                    val firstImage = p.getImagesList().firstOrNull() ?: ""
                    "${p.id}|${p.title}|$firstImage|${cartItem.colorName}|${cartItem.selectedSize}|${cartItem.quantity}|${p.sellingPrice}"
                } else null
            }.joinToString(";")

            val addressString = "${currentAddress.fullName}\n" +
                    "${currentAddress.addressLine1}, ${currentAddress.addressLine2}\n" +
                    "${currentAddress.city}, ${currentAddress.state} - ${currentAddress.pincode}\n" +
                    "Phone: ${currentAddress.phone}"

            val order = Order(
                orderId = generatedOrderId,
                orderDate = System.currentTimeMillis(),
                itemsJson = itemsString,
                shippingAddressJson = addressString,
                paymentMethod = paymentMethod,
                paymentId = paymentId,
                paymentStatus = paymentStatus,
                couponAppliedCode = _appliedCoupon.value?.code ?: "",
                couponDiscount = couponDiscount,
                subtotal = subtotal,
                discount = discount,
                deliveryCharge = deliveryCharge,
                total = total,
                orderStatus = "Ordered",
                estimatedDelivery = System.currentTimeMillis() + 86400000L * 5 // 5 Days delivery
            )

            repository.placeOrder(order)
            repository.clearCart()
            _appliedCoupon.value = null
            onSuccess(generatedOrderId)
        }
    }

    fun cancelOrder(orderId: Int) {
        viewModelScope.launch {
            repository.cancelOrder(orderId)
        }
    }

    // Reviews
    fun getReviewsForProduct(productId: Int): Flow<List<Review>> = repository.getReviewsForProduct(productId)
    
    fun addReview(productId: Int, rating: Int, text: String) {
        viewModelScope.launch {
            val pName = profile.value?.fullName ?: "Anonymous Purchaser"
            val review = Review(
                productId = productId,
                userName = pName,
                rating = rating,
                reviewDate = System.currentTimeMillis(),
                text = text
            )
            repository.addReview(review)
        }
    }

    // Recently Viewed
    fun addToRecentlyViewed(productId: Int) {
        val current = _recentlyViewedIds.value.toMutableList()
        current.remove(productId)
        current.add(0, productId)
        if (current.size > 8) {
            current.removeAt(current.size - 1)
        }
        _recentlyViewedIds.value = current
    }

    // Search and Filters management
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSubcategoryFilter(subcat: String) {
        val current = _selectedSubcategories.value.toSet()
        if (current.contains(subcat)) {
            _selectedSubcategories.value = current - subcat
        } else {
            _selectedSubcategories.value = current + subcat
        }
    }

    fun toggleSizeFilter(size: String) {
        val current = _selectedSizes.value.toSet()
        if (current.contains(size)) {
            _selectedSizes.value = current - size
        } else {
            _selectedSizes.value = current + size
        }
    }

    fun toggleColorFilter(color: String) {
        val current = _selectedColors.value.toSet()
        if (current.contains(color)) {
            _selectedColors.value = current - color
        } else {
            _selectedColors.value = current + color
        }
    }

    fun setDiscountFilter(discount: Int?) {
        _selectedDiscount.value = discount
    }

    fun setRatingFilter(rating: Float?) {
        _selectedRating.value = rating
    }

    fun setPriceRangeFilter(range: ClosedFloatingPointRange<Float>) {
        _priceRange.value = range
    }

    fun setSortType(sort: String) {
        _sortType.value = sort
    }

    fun clearAllFilters() {
        _selectedSubcategories.value = emptySet()
        _priceRange.value = 0f..10000f
        _selectedSizes.value = emptySet()
        _selectedColors.value = emptySet()
        _selectedDiscount.value = null
        _selectedRating.value = null
        _sortType.value = "Relevance"
    }
}
