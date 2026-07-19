package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // "menswear", "kidswear", "footwear"
    val subcategory: String,
    val mrp: Double,
    val sellingPrice: Double,
    val discountPercent: Int,
    val imagesJson: String, // Comma-separated URLs
    val colorsJson: String, // Comma-separated "Name:Hex"
    val sizesJson: String,  // Comma-separated "Size:Stock"
    val material: String,
    val careInstructions: String,
    val fit: String,
    val neckType: String,
    val sleeveType: String,
    val pattern: String,
    val occasion: String,
    val averageRating: Double,
    val ratingCount: Int,
    val isNewArrival: Boolean,
    val isFeatured: Boolean,
    val isBestseller: Boolean,
    val isOnSale: Boolean,
    val totalStock: Int
) {
    fun getImagesList(): List<String> {
        return if (imagesJson.isEmpty()) emptyList() else imagesJson.split(",")
    }

    fun getColorsList(): List<Pair<String, String>> {
        if (colorsJson.isEmpty()) return emptyList()
        return colorsJson.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) parts[0] to parts[1] else null
        }
    }

    fun getSizesList(): List<Pair<String, Int>> {
        if (sizesJson.isEmpty()) return emptyList()
        return sizesJson.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) parts[0] to (parts[1].toIntOrNull() ?: 0) else null
        }
    }
}

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val colorName: String,
    val selectedSize: String,
    val quantity: Int
)

@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey val productId: Int
)

@Entity(tableName = "user_addresses")
data class UserAddress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val phone: String,
    val addressLine1: String,
    val addressLine2: String,
    val city: String,
    val state: String,
    val pincode: String,
    val landmark: String,
    val addressType: String, // "Home", "Office"
    val isDefault: Boolean
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: String, // e.g. "RANGE-2026-10293"
    val orderDate: Long,
    val itemsJson: String, // Custom structured: "productId|title|image|color|size|quantity|price" delimited by ";"
    val shippingAddressJson: String, // Formatted address fields
    val paymentMethod: String,
    val paymentId: String,
    val paymentStatus: String, // "Success", "COD"
    val couponAppliedCode: String,
    val couponDiscount: Double,
    val subtotal: Double,
    val discount: Double,
    val deliveryCharge: Double,
    val total: Double,
    val orderStatus: String, // "Ordered", "Shipped", "Out for Delivery", "Delivered", "Cancelled"
    val estimatedDelivery: Long
) {
    fun getOrderItems(): List<OrderItem> {
        if (itemsJson.isEmpty()) return emptyList()
        return itemsJson.split(";").mapNotNull {
            val parts = it.split("|")
            if (parts.size >= 7) {
                OrderItem(
                    productId = parts[0].toIntOrNull() ?: 0,
                    title = parts[1],
                    image = parts[2],
                    color = parts[3],
                    size = parts[4],
                    quantity = parts[5].toIntOrNull() ?: 1,
                    price = parts[6].toDoubleOrNull() ?: 0.0
                )
            } else null
        }
    }
}

data class OrderItem(
    val productId: Int,
    val title: String,
    val image: String,
    val color: String,
    val size: String,
    val quantity: Int,
    val price: Double
)

@Entity(tableName = "coupons")
data class Coupon(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String,
    val description: String,
    val discountType: String, // "percentage", "flat"
    val discountValue: Double,
    val minOrderAmount: Double,
    val maxDiscount: Double
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val userName: String,
    val rating: Int,
    val reviewDate: Long,
    val text: String
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single profile row
    val fullName: String,
    val email: String,
    val phone: String,
    val gender: String,
    val dob: String,
    val profilePicUrl: String
)
