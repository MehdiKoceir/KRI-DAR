package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.Wilaya
import com.example.ui.components.CategoryPill
import com.example.ui.components.PropertyCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    properties: List<Property>,
    favoriteIds: List<String>,
    wilayas: List<Wilaya>,
    selectedWilaya: String,
    onWilayaChange: (String) -> Unit,
    onPropertyClick: (String) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onSearchClick: (String) -> Unit,
    onMapClick: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<PropertyCategory?>(null) }
    var showWilayaDialog by remember { mutableStateOf(false) }

    val quickLocations = listOf("Blida", "Alger", "Oran", "Bab Ezzouar", "Ouled Yaich", "Cheraga")

    val categories = listOf(
        PropertyCategory.APARTMENT to "Apartment",
        PropertyCategory.HOUSE to "House",
        PropertyCategory.VILLA to "Villa",
        PropertyCategory.STUDIO to "Studio",
        PropertyCategory.ROOM to "Room",
        PropertyCategory.COMMERCIAL to "Commercial"
    )

    val filteredProperties = remember(properties, selectedWilaya, selectedCategory) {
        properties.filter { prop ->
            val matchWilaya = prop.wilaya.equals(selectedWilaya, ignoreCase = true) ||
                    prop.commune.contains(selectedWilaya, ignoreCase = true) ||
                    selectedWilaya == "All Algeria"
            val matchCategory = selectedCategory == null || prop.category == selectedCategory
            matchWilaya && matchCategory
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = BackgroundLight,
                contentColor = TextPrimary
            ) {
                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)) {
                    // Header Row: Location & User Avatar / Map Action
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "LOCATION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { showWilayaDialog = true }
                                    .padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "📍 $selectedWilaya, Algeria",
                                    fontSize = 17.sp,
                                    color = IndigoPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Wilaya",
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Map Action Button
                            IconButton(
                                onClick = onMapClick,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(OrangeAccent)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Map view",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Profile Initial Circle
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Indigo100)
                                    .border(1.dp, IndigoPrimary.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Text(
                                    text = "MB",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Find your next home 🏠",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Large Search Bar (Professional Polish Style)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SurfaceLight,
                        shadowElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSearchClick("") }
                            .testTag("home_search_bar")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Where do you want to live?",
                                fontSize = 14.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.weight(1f))

                            // Indigo Tune / Filter Icon Box
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(IndigoPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filter",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundLight)
        ) {
            // Category Selector Section ("Categories")
            item {
                Column(modifier = Modifier.padding(top = 10.dp, bottom = 12.dp)) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            CategoryPill(
                                title = "All Types",
                                icon = Icons.Default.Apps,
                                isSelected = selectedCategory == null,
                                onClick = { selectedCategory = null }
                            )
                        }
                        items(categories) { (cat, name) ->
                            val icon = when (cat) {
                                PropertyCategory.APARTMENT -> Icons.Default.Apartment
                                PropertyCategory.HOUSE -> Icons.Default.Home
                                PropertyCategory.VILLA -> Icons.Default.Villa
                                PropertyCategory.STUDIO -> Icons.Default.Bed
                                PropertyCategory.ROOM -> Icons.Default.SingleBed
                                PropertyCategory.COMMERCIAL -> Icons.Default.Storefront
                                else -> Icons.Default.Domain
                            }
                            CategoryPill(
                                title = name,
                                icon = icon,
                                isSelected = selectedCategory == cat,
                                onClick = { selectedCategory = if (selectedCategory == cat) null else cat }
                            )
                        }
                    }
                }
            }

            // Quick Popular Locations Chips
            item {
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "POPULAR LOCATIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickLocations) { loc ->
                            val isSelected = selectedWilaya.equals(loc, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onWilayaChange(loc) },
                                label = { Text(loc, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isSelected) Color.White else OrangeAccent
                                    )
                                },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrangeAccent,
                                    selectedLabelColor = Color.White,
                                    containerColor = SurfaceLight,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }

            // Trust Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = VerificationGreenLight.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VerificationGreen.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(VerificationGreen)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Trust",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "✓ Anti-Scam Verified Marketplace",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerificationGreen
                            )
                            Text(
                                text = "Every property is verified for fair price and legal landlord ownership.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Properties Section Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "Properties near you",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "See all (${filteredProperties.size})",
                        fontSize = 13.sp,
                        color = IndigoPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onSearchClick("") }
                    )
                }
            }

            // Properties List
            if (filteredProperties.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = OrangeAccent,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No properties found in $selectedWilaya",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try selecting 'All Algeria' or clearing your category filters.",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { onWilayaChange("All Algeria") },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                            ) {
                                Text("Show All Algeria")
                            }
                        }
                    }
                }
            } else {
                items(filteredProperties) { prop ->
                    PropertyCard(
                        property = prop,
                        isFavorite = favoriteIds.contains(prop.id),
                        onCardClick = { onPropertyClick(prop.id) },
                        onFavoriteToggle = { onFavoriteToggle(prop.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    // Select Wilaya Dialog
    if (showWilayaDialog) {
        AlertDialog(
            onDismissRequest = { showWilayaDialog = false },
            title = { Text("Select Wilaya 🇩🇿", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Choose your preferred location:", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        item {
                            ListItem(
                                headlineContent = { Text("All Algeria", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.clickable {
                                    onWilayaChange("All Algeria")
                                    showWilayaDialog = false
                                }
                            )
                            Divider()
                        }
                        items(wilayas) { w ->
                            ListItem(
                                headlineContent = { Text("${w.code} - ${w.nameFr} (${w.nameAr})") },
                                modifier = Modifier.clickable {
                                    onWilayaChange(w.nameFr)
                                    showWilayaDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWilayaDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
