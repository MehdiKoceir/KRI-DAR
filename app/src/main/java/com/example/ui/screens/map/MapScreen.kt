package com.example.ui.screens.map

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.VerificationStatus
import com.example.ui.components.formatDzd
import com.example.ui.theme.*
import kotlin.math.roundToInt

/**
 * Algerian Regional Hub for quick geographical jumping on the map.
 */
data class AlgeriaCityHub(
    val id: String,
    val name: String,
    val wilayaName: String,
    val latitude: Double,
    val longitude: Double,
    val defaultZoom: Float = 2.4f,
    val flagOrIcon: String = "📍"
)

val ALGERIA_HUBS = listOf(
    AlgeriaCityHub("all", "Toute l'Algérie", "All", 36.20, 3.20, 1.0f, "🇩🇿"),
    AlgeriaCityHub("alger", "Alger", "Alger", 36.7538, 3.0588, 3.2f, "📍"),
    AlgeriaCityHub("blida", "Blida / Mitidja", "Blida", 36.4700, 2.8300, 3.2f, "📍"),
    AlgeriaCityHub("oran", "Oran", "Oran", 35.6987, -0.6349, 3.0f, "📍"),
    AlgeriaCityHub("constantine", "Constantine", "Constantine", 36.3650, 6.6147, 3.0f, "📍"),
    AlgeriaCityHub("bejaia", "Béjaïa", "Béjaïa", 36.7509, 5.0567, 3.0f, "📍"),
    AlgeriaCityHub("annaba", "Annaba", "Annaba", 36.9000, 7.7667, 3.0f, "📍"),
    AlgeriaCityHub("setif", "Sétif", "Sétif", 36.1900, 5.4100, 3.0f, "📍"),
    AlgeriaCityHub("tlemcen", "Tlemcen", "Tlemcen", 34.8783, -1.3150, 3.0f, "📍"),
    AlgeriaCityHub("ghardaia", "Ghardaïa", "Ghardaïa", 32.4900, 3.6736, 2.4f, "📍")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    properties: List<Property>,
    favoriteIds: List<String>,
    onPropertyClick: (String) -> Unit,
    onFavoriteToggle: (String) -> Unit
) {
    val context = LocalContext.current

    // Selected Property State
    var selectedProperty by remember { mutableStateOf<Property?>(properties.firstOrNull()) }

    // Map Navigation & Transformation State
    var centerLat by remember { mutableStateOf(36.20) }
    var centerLon by remember { mutableStateOf(3.20) }
    var zoomLevel by remember { mutableStateOf(1.15f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Filter States
    var selectedHubId by remember { mutableStateOf("all") }
    var selectedCategoryFilter by remember { mutableStateOf<PropertyCategory?>(null) }
    var onlyVerifiedFilter by remember { mutableStateOf(false) }

    // Filter properties based on current criteria
    val filteredProperties = remember(properties, selectedHubId, selectedCategoryFilter, onlyVerifiedFilter) {
        properties.filter { prop ->
            val matchWilaya = if (selectedHubId == "all") true else {
                val hub = ALGERIA_HUBS.firstOrNull { it.id == selectedHubId }
                hub == null || prop.wilaya.equals(hub.wilayaName, ignoreCase = true)
            }
            val matchCat = selectedCategoryFilter == null || prop.category == selectedCategoryFilter
            val matchVerif = !onlyVerifiedFilter || prop.isVerifiedProperty || prop.landlordVerification == VerificationStatus.VERIFIED
            matchWilaya && matchCat && matchVerif
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = SurfaceLight,
                shadowElevation = 2.dp,
                modifier = Modifier.testTag("map_screen_header")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Title Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = "Map Explorer",
                                tint = IndigoPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Carte Interactive 🇩🇿",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Localisez les locations en Algérie",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Badge count of filtered properties
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Indigo100,
                            modifier = Modifier.testTag("map_pins_counter")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${filteredProperties.size} sur la carte",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoPrimary
                                )
                            }
                        }
                    }

                    // Wilaya Navigation Carousel
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ALGERIA_HUBS) { hub ->
                            val isSelected = selectedHubId == hub.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedHubId = hub.id
                                    centerLat = hub.latitude
                                    centerLon = hub.longitude
                                    zoomLevel = hub.defaultZoom
                                    panOffset = Offset.Zero
                                    // If properties exist in this wilaya, select the first one
                                    val firstInHub = properties.firstOrNull { it.wilaya.equals(hub.wilayaName, ignoreCase = true) }
                                    if (firstInHub != null) {
                                        selectedProperty = firstInHub
                                    }
                                },
                                label = {
                                    Text(
                                        text = "${hub.flagOrIcon} ${hub.name}",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = BackgroundLight,
                                    labelColor = TextPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) IndigoPrimary else OutlineBorder
                                )
                            )
                        }
                    }

                    // Secondary Quick Filters Row
                    LazyRow(
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Category: All
                        item {
                            FilterChip(
                                selected = selectedCategoryFilter == null,
                                onClick = { selectedCategoryFilter = null },
                                label = { Text("Tous types", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        // Studios
                        item {
                            FilterChip(
                                selected = selectedCategoryFilter == PropertyCategory.STUDIO,
                                onClick = {
                                    selectedCategoryFilter = if (selectedCategoryFilter == PropertyCategory.STUDIO) null else PropertyCategory.STUDIO
                                },
                                label = { Text("Studios", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        // F2
                        item {
                            FilterChip(
                                selected = selectedCategoryFilter == PropertyCategory.F2,
                                onClick = {
                                    selectedCategoryFilter = if (selectedCategoryFilter == PropertyCategory.F2) null else PropertyCategory.F2
                                },
                                label = { Text("F2", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        // F3
                        item {
                            FilterChip(
                                selected = selectedCategoryFilter == PropertyCategory.F3,
                                onClick = {
                                    selectedCategoryFilter = if (selectedCategoryFilter == PropertyCategory.F3) null else PropertyCategory.F3
                                },
                                label = { Text("F3", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        // Villas
                        item {
                            FilterChip(
                                selected = selectedCategoryFilter == PropertyCategory.VILLA,
                                onClick = {
                                    selectedCategoryFilter = if (selectedCategoryFilter == PropertyCategory.VILLA) null else PropertyCategory.VILLA
                                },
                                label = { Text("Villas", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        // Verified Only
                        item {
                            FilterChip(
                                selected = onlyVerifiedFilter,
                                onClick = { onlyVerifiedFilter = !onlyVerifiedFilter },
                                label = { Text("✓ Vérifiés", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = if (onlyVerifiedFilter) Color.White else VerificationGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("map_screen_container")
        ) {
            // Container holding canvas & gesture tracker
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            zoomLevel = (zoomLevel * zoom).coerceIn(0.65f, 4.5f)
                            panOffset += pan
                        }
                    }
            ) {
                val canvasWidth = constraints.maxWidth.toFloat()
                val canvasHeight = constraints.maxHeight.toFloat()

                // Coordinate Projection Engine
                // Base pixels per degree longitude and latitude at zoom = 1.0f
                val pixelsPerDegree = (canvasWidth / 5.2f) * zoomLevel

                fun projectGeo(lat: Double, lon: Double): Offset {
                    val x = (canvasWidth / 2f) + ((lon - centerLon) * pixelsPerDegree).toFloat() + panOffset.x
                    val y = (canvasHeight / 2f) - ((lat - centerLat) * pixelsPerDegree).toFloat() + panOffset.y
                    return Offset(x, y)
                }

                // 1. Vector Map Canvas (Mediterranean Sea, Algerian Coastline, Highways, Cities)
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("map_canvas")
                ) {
                    // Land Terrain Background
                    drawRect(color = Color(0xFFF7F9FC))

                    // Draw Mountain Relieves (Atlas Tellien organic mountain shading)
                    val atlasPoints = listOf(
                        projectGeo(36.35, 1.30), // Chlef mountains
                        projectGeo(36.42, 2.80), // Blidean Atlas
                        projectGeo(36.45, 3.80), // Djurdjura Kabylie
                        projectGeo(36.50, 5.20), // Babors
                        projectGeo(36.30, 6.60)  // Constantine Highlands
                    )
                    if (atlasPoints.size >= 2) {
                        for (i in 0 until atlasPoints.size - 1) {
                            drawLine(
                                color = Color(0xFFE2E8F0).copy(alpha = 0.8f),
                                start = atlasPoints[i],
                                end = atlasPoints[i + 1],
                                strokeWidth = 24f * zoomLevel.coerceAtMost(2.0f),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Draw Mediterranean Sea & Coastal Bay Contours
                    // Coastline coordinates from West (Morocco border) to East (Tunisia border)
                    val coastGeoCoords = listOf(
                        Pair(35.10, -2.20), // Ghazaouet
                        Pair(35.30, -1.50), // Béni Saf
                        Pair(35.75, -0.70), // Baie d'Oran
                        Pair(35.95, 0.10),  // Mostaganem
                        Pair(36.50, 1.30),  // Ténès
                        Pair(36.60, 2.40),  // Tipaza / Cherchell
                        Pair(36.78, 3.05),  // Baie d'Alger
                        Pair(36.90, 3.90),  // Dellys / Boumerdès
                        Pair(36.88, 4.40),  // Azeffoun
                        Pair(36.76, 5.10),  // Golfe de Béjaïa
                        Pair(36.85, 6.20),  // Jijel Corniche
                        Pair(36.90, 6.90),  // Skikda
                        Pair(36.92, 7.80),  // Golfe d'Annaba
                        Pair(36.90, 8.50)   // El Kala
                    )

                    val seaPath = Path().apply {
                        val firstScreen = projectGeo(coastGeoCoords.first().first, coastGeoCoords.first().second)
                        moveTo(0f, 0f)
                        lineTo(0f, firstScreen.y)

                        coastGeoCoords.forEach { (lat, lon) ->
                            val pt = projectGeo(lat, lon)
                            lineTo(pt.x, pt.y)
                        }

                        val lastScreen = projectGeo(coastGeoCoords.last().first, coastGeoCoords.last().second)
                        lineTo(canvasWidth, lastScreen.y)
                        lineTo(canvasWidth, 0f)
                        close()
                    }

                    // Fill Mediterranean Azure Blue
                    drawPath(seaPath, color = Color(0xFFDCEAF7))

                    // Coastline subtle boundary stroke
                    drawPath(
                        seaPath,
                        color = Color(0xFF93C5FD),
                        style = Stroke(width = 2.5f)
                    )

                    // Draw Major Highway: Autoroute Est-Ouest (A1)
                    val a1Waypoints = listOf(
                        projectGeo(34.88, -1.32), // Tlemcen
                        projectGeo(35.20, -0.63), // Sidi Bel Abbès
                        projectGeo(35.70, -0.63), // Oran
                        projectGeo(36.16, 1.33),  // Chlef
                        projectGeo(36.47, 2.83),  // Blida
                        projectGeo(36.75, 3.05),  // Alger
                        projectGeo(36.38, 3.90),  // Bouira
                        projectGeo(36.19, 5.41),  // Sétif
                        projectGeo(36.36, 6.61),  // Constantine
                        projectGeo(36.90, 7.76)   // Annaba
                    )

                    if (a1Waypoints.size >= 2) {
                        for (i in 0 until a1Waypoints.size - 1) {
                            // Road Underlay
                            drawLine(
                                color = Color(0xFFCBD5E1),
                                start = a1Waypoints[i],
                                end = a1Waypoints[i + 1],
                                strokeWidth = 5f * zoomLevel.coerceIn(0.8f, 2.0f),
                                cap = StrokeCap.Round
                            )
                            // Road Core
                            drawLine(
                                color = Color(0xFF94A3B8),
                                start = a1Waypoints[i],
                                end = a1Waypoints[i + 1],
                                strokeWidth = 2.5f * zoomLevel.coerceIn(0.8f, 2.0f),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Draw Route du Sud: Trans-Saharienne (RN1)
                    val rn1Waypoints = listOf(
                        projectGeo(36.75, 3.05), // Alger
                        projectGeo(36.47, 2.83), // Blida
                        projectGeo(36.26, 2.75), // Médéa
                        projectGeo(34.67, 3.25), // Djelfa
                        projectGeo(33.80, 2.87), // Laghouat
                        projectGeo(32.49, 3.67)  // Ghardaïa
                    )

                    if (rn1Waypoints.size >= 2) {
                        for (i in 0 until rn1Waypoints.size - 1) {
                            drawLine(
                                color = Color(0xFFE2E8F0),
                                start = rn1Waypoints[i],
                                end = rn1Waypoints[i + 1],
                                strokeWidth = 3f * zoomLevel.coerceIn(0.8f, 1.8f),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Draw Cities Anchors & Labels on Map
                    ALGERIA_HUBS.filter { it.id != "all" }.forEach { hub ->
                        val cityPos = projectGeo(hub.latitude, hub.longitude)
                        val isCapital = hub.id == "alger"

                        // City Node Dot
                        drawCircle(
                            color = if (isCapital) IndigoPrimary else Color(0xFF64748B),
                            radius = if (isCapital) 5.5f * zoomLevel.coerceIn(0.8f, 1.5f) else 4f * zoomLevel.coerceIn(0.8f, 1.5f),
                            center = cityPos
                        )
                        drawCircle(
                            color = Color.White,
                            radius = if (isCapital) 2.5f * zoomLevel.coerceIn(0.8f, 1.5f) else 1.5f * zoomLevel.coerceIn(0.8f, 1.5f),
                            center = cityPos
                        )
                    }
                }

                // 2. City Name Labels Overlay (Compose Typography above Canvas)
                Box(modifier = Modifier.fillMaxSize()) {
                    ALGERIA_HUBS.filter { it.id != "all" }.forEach { hub ->
                        val pos = projectGeo(hub.latitude, hub.longitude)
                        // Only show label if inside or near screen
                        if (pos.x in -50f..(canvasWidth + 50f) && pos.y in -30f..(canvasHeight + 30f)) {
                            val isCapital = hub.id == "alger"
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(pos.x.roundToInt() + 8, pos.y.roundToInt() - 14) }
                            ) {
                                Text(
                                    text = if (isCapital) "★ ${hub.name}" else hub.name,
                                    fontSize = if (isCapital) 12.sp else 11.sp,
                                    fontWeight = if (isCapital) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = if (isCapital) IndigoPrimary else TextSecondary,
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    // User GPS Position Indicator (Blida / Alger)
                    val userGpsPos = projectGeo(36.475, 2.835)
                    if (userGpsPos.x in 0f..canvasWidth && userGpsPos.y in 0f..canvasHeight) {
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(userGpsPos.x.roundToInt() - 14, userGpsPos.y.roundToInt() - 14) }
                                .size(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Radar ripple
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(IndigoPrimary.copy(alpha = 0.22f))
                            )
                            // Solid core
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(IndigoPrimary)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }
                    }

                    // 3. Interactive Rental Property Price Pins
                    filteredProperties.forEach { prop ->
                        val pinPos = projectGeo(prop.latitude, prop.longitude)
                        val isSelected = selectedProperty?.id == prop.id

                        // Only render pin if within visible viewport bounds
                        if (pinPos.x in -80f..(canvasWidth + 80f) && pinPos.y in -80f..(canvasHeight + 80f)) {
                            val priceShort = if (prop.priceDzd >= 1000) {
                                "${(prop.priceDzd / 1000).toInt()}k DA"
                            } else {
                                "${prop.priceDzd.toInt()} DA"
                            }

                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            (pinPos.x - 46).roundToInt(),
                                            (pinPos.y - 36).roundToInt()
                                        )
                                    }
                                    .clickable {
                                        selectedProperty = prop
                                    }
                                    .testTag("map_marker_${prop.id}")
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    // Main Badge Pill
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSelected) IndigoPrimary else SurfaceLight,
                                        contentColor = if (isSelected) Color.White else TextPrimary,
                                        shadowElevation = if (isSelected) 8.dp else 3.dp,
                                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder),
                                        modifier = Modifier.animateContentSize()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        ) {
                                            // Verified Check or Category Icon
                                            if (prop.isVerifiedProperty) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Verified",
                                                    tint = if (isSelected) Color.White else VerificationGreen,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Home,
                                                    contentDescription = null,
                                                    tint = if (isSelected) Color.White else IndigoPrimary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                            }

                                            Text(
                                                text = if (isSelected) formatDzd(prop.priceDzd) else priceShort,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    // Downward indicator triangle for selected pin
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .offset(y = (-2).dp)
                                                .clip(CircleShape)
                                                .background(IndigoPrimary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Floating Map Navigation Controls (Top Right)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Zoom In
                FloatingActionButton(
                    onClick = { zoomLevel = (zoomLevel * 1.25f).coerceAtMost(4.5f) },
                    containerColor = SurfaceLight,
                    contentColor = TextPrimary,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("map_zoom_in")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }

                // Zoom Out
                FloatingActionButton(
                    onClick = { zoomLevel = (zoomLevel / 1.25f).coerceAtLeast(0.65f) },
                    containerColor = SurfaceLight,
                    contentColor = TextPrimary,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("map_zoom_out")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                }

                // Recenter My Location (Blida GPS)
                FloatingActionButton(
                    onClick = {
                        centerLat = 36.47
                        centerLon = 2.83
                        zoomLevel = 3.0f
                        panOffset = Offset.Zero
                    },
                    containerColor = SurfaceLight,
                    contentColor = IndigoPrimary,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("map_recenter_button")
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                }

                // Reset to Whole Algeria View
                FloatingActionButton(
                    onClick = {
                        selectedHubId = "all"
                        centerLat = 36.20
                        centerLon = 3.20
                        zoomLevel = 1.15f
                        panOffset = Offset.Zero
                    },
                    containerColor = SurfaceLight,
                    contentColor = TextPrimary,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("map_reset_view_button")
                ) {
                    Icon(Icons.Default.Public, contentDescription = "Full Algeria Overview")
                }
            }

            // 5. Floating Bottom Property Preview Card (When a Property Pin is Selected)
            AnimatedVisibility(
                visible = selectedProperty != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                selectedProperty?.let { prop ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("map_property_preview_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Property Thumbnail
                                val imgName = prop.imageResNames.firstOrNull() ?: ""
                                val resId = context.resources.getIdentifier(imgName, "drawable", context.packageName)

                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Indigo100)
                                ) {
                                    if (resId != 0) {
                                        Image(
                                            painter = painterResource(id = resId),
                                            contentDescription = prop.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Apartment,
                                            contentDescription = null,
                                            tint = IndigoPrimary,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .align(Alignment.Center)
                                        )
                                    }

                                    // Category Tag Overlay
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = IndigoPrimary.copy(alpha = 0.9f),
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(4.dp)
                                    ) {
                                        Text(
                                            text = prop.category.displayName,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Information details
                                Column(modifier = Modifier.weight(1f)) {
                                    // Verified & Location row
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (prop.isVerifiedProperty) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = "Verified",
                                                    tint = VerificationGreen,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "Vérifié",
                                                    color = VerificationGreen,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = prop.rentalType.name.lowercase().capitalize(),
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }

                                        // Close button
                                        IconButton(
                                            onClick = { selectedProperty = null },
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Dismiss",
                                                tint = TextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Title
                                    Text(
                                        text = prop.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // Commune & Wilaya
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${prop.commune}, ${prop.wilaya}",
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            maxLines = 1
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Price
                                    Text(
                                        text = "${formatDzd(prop.priceDzd)} / mois",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = IndigoPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons row: Details & Favorite
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { onPropertyClick(prop.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .testTag("map_view_details_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Voir l'annonce", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                val isFav = favoriteIds.contains(prop.id)
                                IconButton(
                                    onClick = { onFavoriteToggle(prop.id) },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isFav) Color(0xFFFEE2E2) else BackgroundLight)
                                        .border(1.dp, if (isFav) Color(0xFFFCA5A5) else OutlineBorder, RoundedCornerShape(12.dp))
                                        .testTag("map_favorite_button")
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFav) Color(0xFFDC2626) else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
