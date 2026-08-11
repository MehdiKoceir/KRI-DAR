package com.example.ui.screens.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Property
import com.example.ui.components.PropertyCard
import com.example.ui.components.formatDzd
import com.example.ui.theme.*

@Composable
fun MapScreen(
    properties: List<Property>,
    favoriteIds: List<String>,
    onPropertyClick: (String) -> Unit,
    onFavoriteToggle: (String) -> Unit
) {
    var selectedProperty by remember { mutableStateOf<Property?>(properties.firstOrNull()) }
    var zoomLevel by remember { mutableStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    Scaffold(
        topBar = {
            Surface(
                color = SurfaceLight,
                shadowElevation = 3.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = OrangeAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Interactive Map 🇩🇿",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = IndigoPrimaryContainer.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${properties.size} Pins",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE2E8F0))
        ) {
            // Map Canvas Abstraction (Renders streets, green areas, water, and property pins)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            zoomLevel = (zoomLevel * zoom).coerceIn(0.8f, 2.5f)
                            panOffset += pan
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Draw Background Land / Roads Grid Abstraction
                    drawRect(color = Color(0xFFF1F5F9))

                    // Draw Coastline / River curve
                    val riverPath = Path().apply {
                        moveTo(0f, height * 0.15f)
                        cubicTo(
                            width * 0.3f, height * 0.1f,
                            width * 0.6f, height * 0.3f,
                            width, height * 0.25f
                        )
                        lineTo(width, 0f)
                        lineTo(0f, 0f)
                        close()
                    }
                    drawPath(riverPath, color = Color(0xFFBAE6FD)) // Sea / Water Blue

                    // Major A1 Highway Line
                    drawLine(
                        color = Color(0xFFCBD5E1),
                        start = Offset(0f, height * 0.4f),
                        end = Offset(width, height * 0.7f),
                        strokeWidth = 14f * zoomLevel
                    )

                    // Secondary Road Grid
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = Offset(width * 0.2f, height * 0.2f),
                        end = Offset(width * 0.8f, height * 0.9f),
                        strokeWidth = 8f * zoomLevel
                    )
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = Offset(width * 0.7f, height * 0.2f),
                        end = Offset(width * 0.1f, height * 0.8f),
                        strokeWidth = 8f * zoomLevel
                    )
                }

                // Render Pins overlay based on properties coordinates mapping
                Box(modifier = Modifier.fillMaxSize()) {
                    // Render "You" Pin
                    Box(
                        modifier = Modifier
                            .offset(x = 180.dp, y = 280.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PersonPinCircle,
                                contentDescription = "You",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("You", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Render Property Price Markers
                    properties.forEachIndexed { index, prop ->
                        val isSelected = selectedProperty?.id == prop.id
                        val pinX = (60 + (index * 75) % 260).dp
                        val pinY = (120 + (index * 95) % 360).dp

                        val priceShort = "${(prop.priceDzd / 1000).toInt()}k DA"

                        Surface(
                            onClick = { selectedProperty = prop },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) OrangeAccent else IndigoPrimary,
                            contentColor = Color.White,
                            shadowElevation = if (isSelected) 8.dp else 4.dp,
                            modifier = Modifier
                                .offset(x = pinX, y = pinY)
                                .testTag("map_marker_${prop.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.LocationOn else Icons.Default.Home,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = priceShort,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            // Floating Map Zoom Controls
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { zoomLevel = (zoomLevel + 0.2f).coerceAtMost(2.5f) },
                    containerColor = SurfaceLight,
                    contentColor = TextPrimary,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom in")
                }
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(
                    onClick = { zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.8f) },
                    containerColor = SurfaceLight,
                    contentColor = TextPrimary,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom out")
                }
            }

            // Bottom Floating Property Card Preview
            selectedProperty?.let { prop ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 80.dp)
                ) {
                    PropertyCard(
                        property = prop,
                        isFavorite = favoriteIds.contains(prop.id),
                        onCardClick = { onPropertyClick(prop.id) },
                        onFavoriteToggle = { onFavoriteToggle(prop.id) }
                    )
                }
            }
        }
    }
}
