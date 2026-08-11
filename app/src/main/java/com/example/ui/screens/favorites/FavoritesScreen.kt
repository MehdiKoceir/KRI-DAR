package com.example.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Property
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PropertyCard
import com.example.ui.components.formatDzd
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favoriteProperties: List<Property>,
    onPropertyClick: (String) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    var showCompareSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(
                color = SurfaceLight,
                shadowElevation = 2.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Saved Favorites ❤️",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    if (favoriteProperties.size >= 2) {
                        Button(
                            onClick = { showCompareSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("compare_button")
                        ) {
                            Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compare (${favoriteProperties.size.coerceAtMost(3)})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (favoriteProperties.isEmpty()) {
            EmptyStateView(
                title = "No Favorite Properties Saved",
                subtitle = "Tap the heart icon on any property to save it here for quick access and price comparison.",
                icon = Icons.Default.FavoriteBorder,
                actionButtonText = "Explore Properties",
                onActionClick = onSearchClick,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(BackgroundLight)
            ) {
                items(favoriteProperties) { prop ->
                    PropertyCard(
                        property = prop,
                        isFavorite = true,
                        onCardClick = { onPropertyClick(prop.id) },
                        onFavoriteToggle = { onFavoriteToggle(prop.id) }
                    )
                }
            }
        }
    }

    // Property Comparison Sheet
    if (showCompareSheet && favoriteProperties.isNotEmpty()) {
        val compareList = favoriteProperties.take(3)

        ModalBottomSheet(
            onDismissRequest = { showCompareSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Side-by-Side Comparison 📊",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Comparing ${compareList.size} selected properties:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Comparison Matrix Table
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CompareRow(title = "Title", values = compareList.map { it.title.take(15) + "..." })
                    Divider()
                    CompareRow(title = "Price", values = compareList.map { formatDzd(it.priceDzd) }, isHighlight = true)
                    Divider()
                    CompareRow(title = "Location", values = compareList.map { "${it.commune}, ${it.wilaya}" })
                    Divider()
                    CompareRow(title = "Surface", values = compareList.map { "${it.surfaceM2} m²" })
                    Divider()
                    CompareRow(title = "Bedrooms", values = compareList.map { "${it.bedrooms} beds" })
                    Divider()
                    CompareRow(title = "Parking", values = compareList.map { if (it.hasParking) "✅ Yes" else "❌ No" })
                    Divider()
                    CompareRow(title = "Furnished", values = compareList.map { if (it.isFurnished) "✅ Yes" else "❌ No" })
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showCompareSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Matrix")
                }
            }
        }
    }
}

@Composable
private fun CompareRow(title: String, values: List<String>, isHighlight: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            modifier = Modifier.width(70.dp)
        )
        values.forEach { valStr ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                Text(
                    text = valStr,
                    fontSize = 12.sp,
                    fontWeight = if (isHighlight) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (isHighlight) OrangeAccent else TextPrimary
                )
            }
        }
    }
}
