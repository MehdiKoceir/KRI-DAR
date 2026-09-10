package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Rental property types specifically requested for Algerian rental marketplace.
 */
enum class RentalTypeOption(
    val id: String,
    val labelFr: String,
    val labelAr: String,
    val subLabel: String,
    val dbGroup: String?,
    val icon: ImageVector
) {
    ALL("all", "Tous les types", "الكل", "Tous les biens", null, Icons.Default.GridView),
    APARTMENT("apartment", "Appartement", "شقة", "F1, F2, F3, F4, F5", "APARTMENT", Icons.Default.Apartment),
    HOUSE("house", "Maison / Villa", "منزل / فيلا", "Villa, Duplex, R+1", "HOUSE", Icons.Default.Home),
    STUDIO("studio", "Studio", "استوديو", "Studio individuel", "STUDIO", Icons.Default.SingleBed),
    ROOM("room", "Chambre", "غرفة", "Colocation, Chambre", "ROOM", Icons.Default.MeetingRoom);
}

/**
 * Structured state representing active marketplace filters.
 */
data class MarketplaceFilterState(
    val rentalType: RentalTypeOption = RentalTypeOption.ALL,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val city: String = "Toutes les villes",
    val searchQuery: String = "",
    val sortOrder: String = "RECENT" // "RECENT", "PRICE_ASC", "PRICE_DESC"
) {
    val hasActiveFilters: Boolean
        get() = rentalType != RentalTypeOption.ALL ||
                (minPrice != null && minPrice > 10000.0) ||
                (maxPrice != null && maxPrice < 250000.0) ||
                (city.isNotBlank() && city != "Toutes les villes" && city != "All") ||
                searchQuery.isNotBlank()

    val activeFiltersCount: Int
        get() {
            var count = 0
            if (rentalType != RentalTypeOption.ALL) count++
            if (minPrice != null && minPrice > 10000.0) count++
            if (maxPrice != null && maxPrice < 250000.0) count++
            if (city.isNotBlank() && city != "Toutes les villes" && city != "All") count++
            if (searchQuery.isNotBlank()) count++
            return count
        }
}

/**
 * Algerian Wilaya representation for city filtering.
 */
data class AlgerianCityItem(
    val code: String,
    val nameFr: String,
    val nameAr: String
)

val POPULAR_ALGERIAN_CITIES = listOf(
    "Toutes les villes",
    "Alger",
    "Blida",
    "Oran",
    "Constantine",
    "Annaba",
    "Béjaïa",
    "Sétif",
    "Tlemcen"
)

val ALL_ALGERIAN_WILAYAS = listOf(
    AlgerianCityItem("16", "Alger", "الجزائر"),
    AlgerianCityItem("09", "Blida", "البليدة"),
    AlgerianCityItem("31", "Oran", "وهران"),
    AlgerianCityItem("25", "Constantine", "قسنطينة"),
    AlgerianCityItem("23", "Annaba", "عنابة"),
    AlgerianCityItem("06", "Béjaïa", "بجاية"),
    AlgerianCityItem("19", "Sétif", "سطيف"),
    AlgerianCityItem("13", "Tlemcen", "تلمسان"),
    AlgerianCityItem("15", "Tizi Ouzou", "تيزي وزو"),
    AlgerianCityItem("05", "Batna", "باتنة"),
    AlgerianCityItem("02", "Chlef", "الشلف"),
    AlgerianCityItem("17", "Djelfa", "الجلفة"),
    AlgerianCityItem("07", "Biskra", "بسكرة"),
    AlgerianCityItem("27", "Mostaganem", "مستغانم"),
    AlgerianCityItem("26", "Médéa", "المدية"),
    AlgerianCityItem("28", "M'Sila", "المسيلة"),
    AlgerianCityItem("29", "Mascara", "معسكر"),
    AlgerianCityItem("30", "Ouargla", "ورقلة"),
    AlgerianCityItem("35", "Boumerdès", "بومرداس"),
    AlgerianCityItem("42", "Tipaza", "تيبازة"),
    AlgerianCityItem("10", "Bouira", "البويرة"),
    AlgerianCityItem("44", "Aïn Defla", "عين الدفلى"),
    AlgerianCityItem("18", "Jijel", "جيجل"),
    AlgerianCityItem("21", "Skikda", "سكيكدة"),
    AlgerianCityItem("22", "Sidi Bel Abbès", "سيدي بلعباس"),
    AlgerianCityItem("34", "Bordj Bou Arréridj", "برج بوعريريج"),
    AlgerianCityItem("47", "Ghardaïa", "غرداية")
)

/**
 * Compact top filter bar with quick search, rental type pills, and city selector.
 */
@Composable
fun MarketplaceFilterBar(
    filterState: MarketplaceFilterState,
    onFilterChange: (MarketplaceFilterState) -> Unit,
    onOpenFilterSheet: () -> Unit,
    modifier: Modifier = Modifier,
    matchingCount: Int? = null
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceLight)
            .padding(top = 8.dp, bottom = 12.dp)
    ) {
        // Search Input Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = filterState.searchQuery,
                onValueChange = { onFilterChange(filterState.copy(searchQuery = it)) },
                placeholder = {
                    Text(
                        text = "Rechercher quartier, wilaya, mot-clé...",
                        fontSize = 13.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Recherche",
                        tint = KriDarNavy,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (filterState.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onFilterChange(filterState.copy(searchQuery = "")) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Effacer",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KriDarNavy,
                    unfocusedBorderColor = OutlineBorder,
                    focusedContainerColor = SurfaceLight,
                    unfocusedContainerColor = SurfaceVariantLight.copy(alpha = 0.5f)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("marketplace_search_input")
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Filter Sheet Trigger Button with Badge
            Box {
                Button(
                    onClick = onOpenFilterSheet,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (filterState.hasActiveFilters) KriDarNavy else KriDarSoftContainer,
                        contentColor = if (filterState.hasActiveFilters) Color.White else KriDarNavy
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("marketplace_filter_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filtres avancés",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Filtres",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (filterState.activeFiltersCount > 0) {
                    Badge(
                        containerColor = VerificationGreen,
                        contentColor = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                    ) {
                        Text(
                            text = "${filterState.activeFiltersCount}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Quick-Filter Chips: Rental Types (apartment, house, studio, room)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // City Pill indicator
            item {
                AssistChip(
                    onClick = onOpenFilterSheet,
                    label = {
                        Text(
                            text = if (filterState.city.isNotBlank() && filterState.city != "Toutes les villes")
                                "📍 ${filterState.city}" else "📍 Toutes les villes",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (filterState.city != "Toutes les villes") KriDarSoftContainer else SurfaceLight,
                        labelColor = if (filterState.city != "Toutes les villes") KriDarNavy else TextSecondary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (filterState.city != "Toutes les villes") KriDarNavy.copy(alpha = 0.5f) else OutlineBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // Rental Type Filter Chips
            items(RentalTypeOption.values()) { option ->
                val isSelected = filterState.rentalType == option
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onFilterChange(filterState.copy(rentalType = option))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = option.labelFr,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSelected) Color.White else KriDarNavy
                        )
                    },
                    label = {
                        Text(
                            text = option.labelFr,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = KriDarNavy,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceLight,
                        labelColor = TextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) KriDarNavy else OutlineBorder
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("filter_chip_${option.id}")
                )
            }
        }
    }
}

/**
 * Comprehensive Material 3 Modal Bottom Sheet for Marketplace Filtering.
 * Supports:
 * - Rental type (apartment, house, studio, room)
 * - Price range in Algerian Dinar (DZD) with dual slider & presets
 * - City in Algeria with quick selection & 58 Wilayas lookup
 * - Live Room results count indicator
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MarketplaceFilterBottomSheet(
    filterState: MarketplaceFilterState,
    onFilterChange: (MarketplaceFilterState) -> Unit,
    onDismiss: () -> Unit,
    resultsCount: Int? = null,
    modifier: Modifier = Modifier
) {
    // Local copy of filter state being edited
    var localFilter by remember(filterState) { mutableStateOf(filterState) }
    var minPriceInput by remember(localFilter.minPrice) {
        mutableStateOf(localFilter.minPrice?.toInt()?.toString() ?: "10000")
    }
    var maxPriceInput by remember(localFilter.maxPrice) {
        mutableStateOf(localFilter.maxPrice?.toInt()?.toString() ?: "200000")
    }
    var showAllWilayasDialog by remember { mutableStateOf(false) }
    var wilayaSearchText by remember { mutableStateOf("") }

    // Price range slider values
    val currentMin = localFilter.minPrice?.toFloat() ?: 10000f
    val currentMax = localFilter.maxPrice?.toFloat() ?: 200000f
    var sliderRange by remember(currentMin, currentMax) {
        mutableStateOf(currentMin.coerceIn(10000f, 300000f)..currentMax.coerceIn(10000f, 300000f))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceLight,
        dragHandle = { BottomSheetDefaults.DragHandle(color = OutlineBorder) },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 20.dp)
        ) {
            // Header: Title, Active Filter Count, Reset All
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column {
                    Text(
                        text = "Filtres Marketplace 🔍",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Recherchez le logement idéal en Algérie",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                if (localFilter.hasActiveFilters) {
                    TextButton(
                        onClick = {
                            localFilter = MarketplaceFilterState()
                            sliderRange = 10000f..200000f
                            minPriceInput = "10000"
                            maxPriceInput = "200000"
                        },
                        modifier = Modifier.testTag("filter_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Réinitialiser",
                            modifier = Modifier.size(16.dp),
                            tint = KriDarNavy
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Effacer tout",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = KriDarNavy
                        )
                    }
                }
            }

            HorizontalDivider(color = OutlineBorder)

            // Scrollable Content
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // =========================================================================
                // 1. RENTAL TYPE SELECTION (Apartment, House, Studio, Room)
                // =========================================================================
                item {
                    Column(modifier = Modifier.testTag("filter_rental_type_section")) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "1. Type de location",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = localFilter.rentalType.labelFr,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = KriDarNavy
                            )
                        }

                        Text(
                            text = "Sélectionnez le type d'habitation souhaité :",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                        )

                        // 4 Interactive Category Cards (Apartment, House, Studio, Room)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // "Tous les types" full-width pill
                            RentalTypeCard(
                                option = RentalTypeOption.ALL,
                                isSelected = localFilter.rentalType == RentalTypeOption.ALL,
                                onClick = { localFilter = localFilter.copy(rentalType = RentalTypeOption.ALL) }
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RentalTypeCard(
                                    option = RentalTypeOption.APARTMENT,
                                    isSelected = localFilter.rentalType == RentalTypeOption.APARTMENT,
                                    onClick = { localFilter = localFilter.copy(rentalType = RentalTypeOption.APARTMENT) },
                                    modifier = Modifier.weight(1f)
                                )
                                RentalTypeCard(
                                    option = RentalTypeOption.HOUSE,
                                    isSelected = localFilter.rentalType == RentalTypeOption.HOUSE,
                                    onClick = { localFilter = localFilter.copy(rentalType = RentalTypeOption.HOUSE) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RentalTypeCard(
                                    option = RentalTypeOption.STUDIO,
                                    isSelected = localFilter.rentalType == RentalTypeOption.STUDIO,
                                    onClick = { localFilter = localFilter.copy(rentalType = RentalTypeOption.STUDIO) },
                                    modifier = Modifier.weight(1f)
                                )
                                RentalTypeCard(
                                    option = RentalTypeOption.ROOM,
                                    isSelected = localFilter.rentalType == RentalTypeOption.ROOM,
                                    onClick = { localFilter = localFilter.copy(rentalType = RentalTypeOption.ROOM) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // =========================================================================
                // 2. PRICE RANGE IN DZD
                // =========================================================================
                item {
                    Column(modifier = Modifier.testTag("filter_price_section")) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "2. Fourchette de prix (DZD / mois)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = KriDarNavy,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Current Price Range display badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = KriDarSoftContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    Text(text = "Loyer Minimum", fontSize = 11.sp, color = TextSecondary)
                                    Text(
                                        text = "${sliderRange.start.toInt()} DZD",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KriDarNavy
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = KriDarNavy,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "Loyer Maximum", fontSize = 11.sp, color = TextSecondary)
                                    Text(
                                        text = if (sliderRange.endInclusive >= 300000f) "300 000+ DZD" else "${sliderRange.endInclusive.toInt()} DZD",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KriDarNavy
                                    )
                                }
                            }
                        }

                        // Dual Thumb Range Slider
                        RangeSlider(
                            value = sliderRange,
                            onValueChange = { range ->
                                sliderRange = range
                                val minDzd = (range.start / 5000).toInt() * 5000.0
                                val maxDzd = (range.endInclusive / 5000).toInt() * 5000.0
                                localFilter = localFilter.copy(
                                    minPrice = if (minDzd > 10000.0) minDzd else null,
                                    maxPrice = if (maxDzd < 300000.0) maxDzd else null
                                )
                                minPriceInput = minDzd.toInt().toString()
                                maxPriceInput = maxDzd.toInt().toString()
                            },
                            valueRange = 10000f..300000f,
                            steps = 57, // increments of 5000 DZD
                            colors = SliderDefaults.colors(
                                thumbColor = KriDarNavy,
                                activeTrackColor = KriDarNavy,
                                inactiveTrackColor = OutlineBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Quick Price Presets in Algeria
                        Text(
                            text = "Budgets fréquents en Algérie :",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            listOf(
                                PricePreset("< 35k DZD", null, 35000.0, "Étudiant / Petit budget"),
                                PricePreset("35k - 75k DZD", 35000.0, 75000.0, "Standard (F2 / F3)"),
                                PricePreset("75k - 150k DZD", 75000.0, 150000.0, "Standing / Familial"),
                                PricePreset("> 150k DZD", 150000.0, null, "Luxe / Villa")
                            ).forEach { preset ->
                                val isPresetActive = localFilter.minPrice == preset.min && localFilter.maxPrice == preset.max
                                FilterChip(
                                    selected = isPresetActive,
                                    onClick = {
                                        localFilter = localFilter.copy(minPrice = preset.min, maxPrice = preset.max)
                                        val newMin = (preset.min ?: 10000.0).toFloat()
                                        val newMax = (preset.max ?: 300000.0).toFloat()
                                        sliderRange = newMin..newMax
                                    },
                                    label = { Text(preset.label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KriDarNavy,
                                        selectedLabelColor = Color.White,
                                        containerColor = SurfaceVariantLight
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }

                // =========================================================================
                // 3. CITY IN ALGERIA SELECTION
                // =========================================================================
                item {
                    Column(modifier = Modifier.testTag("filter_city_section")) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "3. Ville / Wilaya en Algérie",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = KriDarNavy,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "Ville sélectionnée : ${localFilter.city}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KriDarNavy,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )

                        // Popular Algerian Cities Chips
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            POPULAR_ALGERIAN_CITIES.forEach { city ->
                                val isCitySelected = localFilter.city == city
                                FilterChip(
                                    selected = isCitySelected,
                                    onClick = {
                                        localFilter = localFilter.copy(city = city)
                                    },
                                    label = {
                                        Text(
                                            text = city,
                                            fontSize = 12.sp,
                                            fontWeight = if (isCitySelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KriDarNavy,
                                        selectedLabelColor = Color.White,
                                        containerColor = SurfaceVariantLight
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Trigger to choose among all 58 Wilayas
                        OutlinedButton(
                            onClick = { showAllWilayasDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = KriDarNavy),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(OutlineBorder)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choisir parmi les 58 Wilayas d'Algérie...", fontSize = 13.sp)
                        }
                    }
                }

                // =========================================================================
                // 4. SORT ORDER SELECTION
                // =========================================================================
                item {
                    Column {
                        Text(
                            text = "4. Trier les résultats",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                "RECENT" to "Plus récents",
                                "PRICE_ASC" to "Prix croissant",
                                "PRICE_DESC" to "Prix décroissant"
                            ).forEach { (sortKey, label) ->
                                val isSelected = localFilter.sortOrder == sortKey
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { localFilter = localFilter.copy(sortOrder = sortKey) },
                                    label = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KriDarNavy,
                                        selectedLabelColor = Color.White,
                                        containerColor = SurfaceVariantLight
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = OutlineBorder)

            // Bottom CTA: Live Apply Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("Annuler")
                }

                Button(
                    onClick = {
                        onFilterChange(localFilter)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KriDarNavy),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("filter_apply_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (resultsCount != null) "Afficher ($resultsCount logements)" else "Appliquer les filtres",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Modal Dialog to Select from All 58 Wilayas of Algeria
    if (showAllWilayasDialog) {
        AlertDialog(
            onDismissRequest = { showAllWilayasDialog = false },
            title = {
                Text(
                    text = "Wilayas d'Algérie (58)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = wilayaSearchText,
                        onValueChange = { wilayaSearchText = it },
                        placeholder = { Text("Filtrer par nom ou numéro...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    val filteredWilayas = remember(wilayaSearchText) {
                        if (wilayaSearchText.isBlank()) ALL_ALGERIAN_WILAYAS
                        else ALL_ALGERIAN_WILAYAS.filter {
                            it.nameFr.contains(wilayaSearchText, ignoreCase = true) ||
                                    it.code.contains(wilayaSearchText) ||
                                    it.nameAr.contains(wilayaSearchText)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        item {
                            ListItem(
                                headlineContent = { Text("Toutes les villes", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.clickable {
                                    localFilter = localFilter.copy(city = "Toutes les villes")
                                    showAllWilayasDialog = false
                                }
                            )
                            HorizontalDivider()
                        }
                        items(filteredWilayas) { wilaya ->
                            ListItem(
                                headlineContent = { Text("${wilaya.code} - ${wilaya.nameFr}") },
                                supportingContent = { Text(wilaya.nameAr, fontSize = 11.sp, color = TextMuted) },
                                trailingContent = {
                                    if (localFilter.city.equals(wilaya.nameFr, ignoreCase = true)) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = KriDarNavy)
                                    }
                                },
                                modifier = Modifier.clickable {
                                    localFilter = localFilter.copy(city = wilaya.nameFr)
                                    showAllWilayasDialog = false
                                }
                            )
                            HorizontalDivider(color = OutlineBorder.copy(alpha = 0.5f))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAllWilayasDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }
}

/**
 * Interactive card component for each rental type option.
 */
@Composable
private fun RentalTypeCard(
    option: RentalTypeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) KriDarNavy else SurfaceVariantLight,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) KriDarNavy else OutlineBorder
        ),
        modifier = modifier
            .height(56.dp)
            .testTag("filter_type_${option.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Color.White.copy(alpha = 0.2f) else KriDarSoftContainer)
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = option.labelFr,
                    tint = if (isSelected) Color.White else KriDarNavy,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.labelFr,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) Color.White else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = option.subLabel,
                    fontSize = 10.sp,
                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Sélectionné",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Data class for price preset chip.
 */
private data class PricePreset(
    val label: String,
    val min: Double?,
    val max: Double?,
    val subtitle: String
)
