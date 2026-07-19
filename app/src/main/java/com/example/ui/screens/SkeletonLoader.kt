package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BrandBorderGrey
import com.example.ui.theme.BrandLightGrey
import com.example.ui.theme.BrandWhite

/**
 * Creates a premium linear shimmer brush with a smooth animation loop.
 */
@Composable
fun shimmerBrush(
    showShimmer: Boolean = true,
    targetValue: Float = 1000f
): Brush {
    return if (showShimmer) {
        val transition = rememberInfiniteTransition(label = "shimmer_transition")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_animation"
        )
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFF3F4F6), // Warm light gray (Tailwind gray-100)
                Color(0xFFE5E7EB), // Shimmer pulse highlight (Tailwind gray-200)
                Color(0xFFF3F4F6)  // Warm light gray
            ),
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}

/**
 * Convenience modifier to apply clipping and a shimmering background brush.
 */
@Composable
fun Modifier.shimmerBackground(
    showShimmer: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(4.dp)
): Modifier = this.then(
    Modifier
        .clip(shape)
        .background(shimmerBrush(showShimmer))
)

/**
 * Skeleton component mimicking a single grid-styled ProductCard.
 */
@Composable
fun GridProductCardSkeleton(
    modifier: Modifier = Modifier,
    imageHeight: Dp = 190.dp,
    cardWidth: Dp? = 160.dp
) {
    val cardModifier = if (cardWidth != null) {
        modifier.width(cardWidth)
    } else {
        modifier.fillMaxWidth()
    }

    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(containerColor = BrandWhite),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, BrandBorderGrey)
    ) {
        Column {
            // Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .shimmerBackground()
            )

            // Details Placeholders
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                // Category/Brand tag
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(10.dp)
                        .shimmerBackground(shape = RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Title
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(14.dp)
                        .shimmerBackground(shape = RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Price Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(12.dp)
                            .shimmerBackground(shape = RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .width(30.dp)
                            .height(10.dp)
                            .shimmerBackground(shape = RoundedCornerShape(2.dp))
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Progress Indicator placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .shimmerBackground(shape = RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(8.dp)
                        .shimmerBackground(shape = RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

/**
 * Skeleton component mimicking a single list-styled ProductCard.
 */
@Composable
fun ListProductCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BrandWhite),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, BrandBorderGrey)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Image Box
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .shimmerBackground()
            )

            // Text Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                // Brand tag
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(9.dp)
                        .shimmerBackground(shape = RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Title
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(13.dp)
                        .shimmerBackground(shape = RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Description
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(10.dp)
                        .shimmerBackground(shape = RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Price line
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(55.dp)
                            .height(12.dp)
                            .shimmerBackground(shape = RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .width(35.dp)
                            .height(10.dp)
                            .shimmerBackground(shape = RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

/**
 * Full skeleton screen representing the HomeScreen.
 */
@Composable
fun HomeScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandLightGrey)
    ) {
        // Announcement Bar placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .shimmerBackground(shape = RoundedCornerShape(0.dp))
        )

        // Hero Banner placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(16.dp)
                .shimmerBackground(shape = RoundedCornerShape(4.dp))
        )

        // Shop by Category section title
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .width(120.dp)
                .height(16.dp)
                .shimmerBackground(shape = RoundedCornerShape(2.dp))
        )

        // Shop by Category circular nodes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(4) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .shimmerBackground(shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(45.dp)
                            .height(10.dp)
                            .shimmerBackground(shape = RoundedCornerShape(2.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trending Now Section
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .width(100.dp)
                .height(16.dp)
                .shimmerBackground(shape = RoundedCornerShape(2.dp))
        )

        // Horizontal Grid card row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                GridProductCardSkeleton()
            }
        }
    }
}

/**
 * Full skeleton screen representing the CategoryScreen.
 */
@Composable
fun CategoryScreenSkeleton(
    isListView: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandLightGrey)
    ) {
        // Header Filter Bar placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .shimmerBackground(shape = RoundedCornerShape(0.dp))
        )

        // Horizontal pill row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .width(75.dp)
                        .height(30.dp)
                        .shimmerBackground(shape = RoundedCornerShape(15.dp))
                )
            }
        }

        // Product list/grid of card skeletons
        if (isListView) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(4) {
                    ListProductCardSkeleton()
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(6) {
                    GridProductCardSkeleton(cardWidth = null)
                }
            }
        }
    }
}

/**
 * Full skeleton screen representing the WishlistScreen.
 */
@Composable
fun WishlistScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandLightGrey)
    ) {
        // Top count banner placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .shimmerBackground(shape = RoundedCornerShape(0.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Grid of wishlist card skeletons
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = false
        ) {
            items(4) {
                GridProductCardSkeleton(imageHeight = 180.dp, cardWidth = null)
            }
        }
    }
}
