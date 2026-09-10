package com.example.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.model.PropertyCategory
import com.example.data.model.RentalType
import com.example.data.repository.KriDarRepository
import com.example.ui.components.*
import com.example.ui.theme.*

enum class PropertySortOrder(val title: String, val shortLabel: String, val dbOrder: String) {
    RECENT("Plus récents", "Récents", "RECENT"),
    PRICE_LOW_TO_HIGH("Prix croissant", "Prix croissant", "PRICE_ASC"),
    PRICE_HIGH_TO_LOW("Prix décroissant", "Prix décroissant", "PRICE_DESC")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    properties: List<Property> = emptyList(),
    favoriteIds: List<String> = emptyList(),
    initialQuery: String = "",
    repository: KriDarRepository? = null,
    onPropertyClick: (String) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onMapClick: () -> Unit = {}
) {
    var filterState by remember {
        mutableStateOf(MarketplaceFilterState(searchQuery = initialQuery))
    }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Live Room database query when repository is available
    val roomFilteredProperties by remember(filterState, repository) {
        if (repository != null) {
            repository.filterMarketplacePropertiesWithRoom(
                city = if (filterState.city == "Toutes les villes" || filterState.city.isBlank()) null else filterState.city,
                minPrice = filterState.minPrice,
                maxPrice = filterState.maxPrice,
                rentalType = filterState.rentalType.dbGroup,
                searchQuery = if (filterState.searchQuery.isBlank()) null else filterState.searchQuery,
                sortOrder = filterState.sortOrder
            )
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    // In-memory fallback if repository is null
    val inMemoryFilteredProperties = remember(properties, filterState) {
        if (repository == null) {
            val q = filterState.searchQuery.trim()
            val list = properties.filter { prop ->
                val matchQuery = q.isEmpty() ||
                        prop.title.contains(q, ignoreCase = true) ||
                        prop.description.contains(q, ignoreCase = true) ||
                        prop.wilaya.contains(q, ignoreCase = true) ||
                        prop.commune.contains(q, ignoreCase = true) ||
                        prop.neighborhood.contains(q, ignoreCase = true)

                val matchCity = filterState.city == "Toutes les villes" ||
                        filterState.city.isBlank() ||
                        prop.wilaya.contains(filterState.city, ignoreCase = true) ||
                        prop.commune.contains(filterState.city, ignoreCase = true)

                val matchMin = filterState.minPrice == null || prop.priceDzd >= filterState.minPrice!!
                val matchMax = filterState.maxPrice == null || prop.priceDzd <= filterState.maxPrice!!

                val matchRental = when (filterState.rentalType) {
                    RentalTypeOption.ALL -> true
                    RentalTypeOption.APARTMENT -> prop.category in listOf(
                        PropertyCategory.APARTMENT, PropertyCategory.F1, PropertyCategory.F2,
                        PropertyCategory.F3, PropertyCategory.F4, PropertyCategory.F5
                    )
                    RentalTypeOption.HOUSE -> prop.category in listOf(
                        PropertyCategory.HOUSE, PropertyCategory.VILLA, PropertyCategory.DUPLEX
                    )
                    RentalTypeOption.STUDIO -> prop.category in listOf(PropertyCategory.STUDIO, PropertyCategory.F1)
                    RentalTypeOption.ROOM -> prop.category == PropertyCategory.ROOM || prop.rentalType == RentalType.ROOM
                }

                matchQuery && matchCity && matchMin && matchMax && matchRental
            }

            when (filterState.sortOrder) {
                "PRICE_ASC" -> list.sortedBy { it.priceDzd }
                "PRICE_DESC" -> list.sortedByDescending { it.priceDzd }
                else -> list.sortedByDescending { it.createdAtTimestamp }
            }
        } else {
            emptyList()
        }
    }

    val displayList = if (repository != null) roomFilteredProperties else inMemoryFilteredProperties

    Scaffold(
        topBar = {
            Surface(
                color = SurfaceLight,
                shadowElevation = 2.dp
            ) {
                Column {
                    MarketplaceFilterBar(
                        filterState = filterState,
                        onFilterChange = { filterState = it },
                        onOpenFilterSheet = { showFilterSheet = true },
                        matchingCount = displayList.size
                    )
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
                            text = "Marketplace Immobilier Algérie 🇩🇿",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary
                        )
                        Text(
                            text = "Loyer moyen en Algérie : ~75 000 DA/mois • Requêtes Room directes",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Results Heading with Sort and Map options
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${displayList.size} Logements trouvés",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Sort Dropdown Button
                    Box {
                        FilledTonalButton(
                            onClick = { showSortMenu = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = SurfaceLight,
                                contentColor = TextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("sort_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Trier",
                                tint = OrangeAccent,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val currentSortLabel = when (filterState.sortOrder) {
                                "PRICE_ASC" -> "Prix croissant"
                                "PRICE_DESC" -> "Prix décroissant"
                                else -> "Récents"
                            }
                            Text(currentSortLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(SurfaceLight)
                        ) {
                            listOf(
                                "RECENT" to "Plus récents",
                                "PRICE_ASC" to "Prix croissant",
                                "PRICE_DESC" to "Prix décroissant"
                            ).forEach { (orderKey, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (filterState.sortOrder == orderKey) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = OrangeAccent,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            Text(
                                                text = label,
                                                fontWeight = if (filterState.sortOrder == orderKey) FontWeight.Bold else FontWeight.Normal,
                                                color = if (filterState.sortOrder == orderKey) OrangeAccent else TextPrimary
                                            )
                                        }
                                    },
                                    onClick = {
                                        filterState = filterState.copy(sortOrder = orderKey)
                                        showSortMenu = false
                                    },
                                    modifier = Modifier.testTag("sort_option_${orderKey.lowercase()}")
                                )
                            }
                        }
                    }

                    // Map Button
                    FilledTonalButton(
                        onClick = onMapClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Indigo100,
                            contentColor = IndigoPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("search_view_on_map_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Map view",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Carte", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (filterState.hasActiveFilters) {
                        TextButton(
                            onClick = { filterState = MarketplaceFilterState() },
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Text("Effacer", fontSize = 12.sp, color = IndigoPrimary)
                        }
                    }
                }
            }

            if (displayList.isEmpty()) {
                EmptyStateView(
                    title = "Aucun logement trouvé",
                    subtitle = "Essayez d'ajuster votre ville, type de location (appartement, maison, studio, chambre) ou votre budget.",
                    actionButtonText = "Réinitialiser les filtres",
                    onActionClick = {
                        filterState = MarketplaceFilterState()
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayList) { prop ->
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

    // Comprehensive Material 3 Filter Bottom Sheet
    if (showFilterSheet) {
        MarketplaceFilterBottomSheet(
            filterState = filterState,
            onFilterChange = { filterState = it },
            onDismiss = { showFilterSheet = false },
            resultsCount = displayList.size
        )
    }
}

