package com.example.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.RentalType
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PropertyCard
import com.example.ui.components.formatDzd
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    properties: List<Property>,
    favoriteIds: List<String>,
    initialQuery: String = "",
    onPropertyClick: (String) -> Unit,
    onFavoriteToggle: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf(initialQuery) }
    var selectedCategory by remember { mutableStateOf<PropertyCategory?>(null) }
    var selectedRentalType by remember { mutableStateOf<RentalType?>(null) }
    var maxPriceDzd by remember { mutableStateOf(200000.0) }
    var minBedrooms by remember { mutableStateOf(0) }
    var onlyFurnished by remember { mutableStateOf(false) }
    var onlyVerified by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val filteredList = remember(
        properties, searchQuery, selectedCategory, selectedRentalType,
        maxPriceDzd, minBedrooms, onlyFurnished, onlyVerified
    ) {
        properties.filter { prop ->
            val matchQuery = searchQuery.isBlank() ||
                    prop.title.contains(searchQuery, ignoreCase = true) ||
                    prop.wilaya.contains(searchQuery, ignoreCase = true) ||
                    prop.commune.contains(searchQuery, ignoreCase = true) ||
                    prop.neighborhood.contains(searchQuery, ignoreCase = true)
            val matchCategory = selectedCategory == null || prop.category == selectedCategory
            val matchRental = selectedRentalType == null || prop.rentalType == selectedRentalType
            val matchPrice = prop.priceDzd <= maxPriceDzd
            val matchBeds = prop.bedrooms >= minBedrooms
            val matchFurnished = !onlyFurnished || prop.isFurnished
            val matchVerified = !onlyVerified || prop.isVerifiedProperty

            matchQuery && matchCategory && matchRental && matchPrice && matchBeds && matchFurnished && matchVerified
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = SurfaceLight,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Search Bar Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by Wilaya, Commune, or Title...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OrangeAccent) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeAccent,
                            unfocusedBorderColor = OutlineBorder
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input_field")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filter Action Pills Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { showFilterSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("filter_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Filters", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        // Max Price Pill
                        AssistChip(
                            onClick = { showFilterSheet = true },
                            label = { Text("≤ ${formatDzd(maxPriceDzd)}", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = OrangeAccent
                                )
                            }
                        )

                        // Verified Only Pill
                        FilterChip(
                            selected = onlyVerified,
                            onClick = { onlyVerified = !onlyVerified },
                            label = { Text("Verified", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (onlyVerified) Color.White else EmeraldTrust
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldTrust,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundLight)
        ) {
            // Price Intelligence Banner
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoPrimaryContainer.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Price Intel",
                        tint = IndigoPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Rental Price Intelligence 📊",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary
                        )
                        Text(
                            text = "Average F3 rent in Algeria: ~82 000 DA/mo (Range: 70k - 95k DA)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Results Heading
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Found ${filteredList.size} Properties",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (searchQuery.isNotEmpty() || selectedCategory != null || maxPriceDzd < 200000.0 || onlyVerified) {
                    TextButton(onClick = {
                        searchQuery = ""
                        selectedCategory = null
                        selectedRentalType = null
                        maxPriceDzd = 200000.0
                        minBedrooms = 0
                        onlyFurnished = false
                        onlyVerified = false
                    }) {
                        Text("Reset Filters", fontSize = 12.sp, color = OrangeAccent)
                    }
                }
            }

            if (filteredList.isEmpty()) {
                EmptyStateView(
                    title = "No Properties Found",
                    subtitle = "Try adjusting your search terms or relaxing your price/bedroom filters.",
                    actionButtonText = "Reset All Filters",
                    onActionClick = {
                        searchQuery = ""
                        selectedCategory = null
                        selectedRentalType = null
                        maxPriceDzd = 200000.0
                        minBedrooms = 0
                        onlyFurnished = false
                        onlyVerified = false
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList) { prop ->
                        PropertyCard(
                            property = prop,
                            isFavorite = favoriteIds.contains(prop.id),
                            onCardClick = { onPropertyClick(prop.id) },
                            onFavoriteToggle = { onFavoriteToggle(prop.id) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }

    // Filter Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Filter Properties 🏡",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Max Price Slider
                Text(
                    text = "Max Rent Budget: ${formatDzd(maxPriceDzd)} / month",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Slider(
                    value = maxPriceDzd.toFloat(),
                    onValueChange = { maxPriceDzd = it.toDouble() },
                    valueRange = 20000f..200000f,
                    steps = 18,
                    colors = SliderDefaults.colors(thumbColor = OrangeAccent, activeTrackColor = OrangeAccent)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Bedrooms Selector
                Text(
                    text = "Minimum Bedrooms:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "Any", 1 to "1+", 2 to "2+", 3 to "3+", 4 to "4+").forEach { (count, label) ->
                        FilterChip(
                            selected = minBedrooms == count,
                            onClick = { minBedrooms = count },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Checkboxes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = onlyFurnished,
                        onCheckedChange = { onlyFurnished = it },
                        colors = CheckboxDefaults.colors(checkedColor = OrangeAccent)
                    )
                    Text("Furnished properties only", fontSize = 14.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = onlyVerified,
                        onCheckedChange = { onlyVerified = it },
                        colors = CheckboxDefaults.colors(checkedColor = EmeraldTrust)
                    )
                    Text("Verified Landlords & Listings only", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showFilterSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Filters (${filteredList.size} Results)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
