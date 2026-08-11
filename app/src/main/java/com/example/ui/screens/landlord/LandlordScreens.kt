package com.example.ui.screens.landlord

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.*
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatCard
import com.example.ui.components.VerificationBadge
import com.example.ui.components.formatDzd
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandlordDashboardScreen(
    landlordUser: User,
    properties: List<Property>,
    visits: List<VisitRequest>,
    onAddPropertyClick: () -> Unit,
    onManageVisitsClick: () -> Unit,
    onPropertyClick: (String) -> Unit
) {
    val activeCount = properties.count { !it.isRented && !it.isPaused }
    val rentedCount = properties.count { it.isRented }
    val totalViews = properties.sumOf { it.viewsCount }

    Scaffold(
        topBar = {
            Surface(
                color = SurfaceLight,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Good morning 👋",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = landlordUser.fullName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Button(
                            onClick = onAddPropertyClick,
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_property_fab")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Post Property", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .background(BackgroundLight)
                .padding(16.dp)
        ) {
            // Analytics Grid
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "Active Listings",
                    value = "$activeCount",
                    icon = Icons.Default.Apartment,
                    accentColor = IndigoPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Rented",
                    value = "$rentedCount",
                    icon = Icons.Default.CheckCircle,
                    accentColor = EmeraldTrust,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "Total Views",
                    value = "$totalViews",
                    icon = Icons.Default.Visibility,
                    accentColor = OrangeAccent,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Visit Requests",
                    value = "${visits.size}",
                    icon = Icons.Default.Event,
                    accentColor = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Landlord Verification Status Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = VerificationGreen,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "✓ Verified Landlord Profile",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldTrust
                        )
                        Text(
                            text = "National ID & Phone Verified. Your listings get 3x higher inquiries.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Listings Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Your Managed Listings", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onManageVisitsClick) {
                    Text("Manage Visits", fontSize = 13.sp, color = OrangeAccent, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (properties.isEmpty()) {
                EmptyStateView(
                    title = "No Properties Posted",
                    subtitle = "Click 'Post Property' above to add your first apartment or villa to Kri/Dar.",
                    actionButtonText = "Post Property Now",
                    onActionClick = onAddPropertyClick
                )
            } else {
                properties.forEach { prop ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { onPropertyClick(prop.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (prop.isRented) Color.Gray else EmeraldTrust
                                ) {
                                    Text(
                                        text = if (prop.isRented) "RENTED" else "ACTIVE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = formatDzd(prop.priceDzd),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeAccent
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(prop.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("📍 ${prop.commune}, ${prop.wilaya}", fontSize = 13.sp, color = TextSecondary)

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("👀 ${prop.viewsCount} views", fontSize = 12.sp, color = TextMuted)
                                Text("Tap to manage >", fontSize = 12.sp, color = IndigoPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitManagementScreen(
    visits: List<VisitRequest>,
    onUpdateVisitStatus: (visitId: String, newStatus: VisitStatus) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            Surface(color = SurfaceLight, shadowElevation = 2.dp) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Visit Requests Calendar 📅", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        if (visits.isEmpty()) {
            EmptyStateView(
                title = "No Visit Requests",
                subtitle = "When tenants request visits for your properties, they will appear here.",
                icon = Icons.Default.Event,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(BackgroundLight)
            ) {
                items(visits) { visit ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = visit.visitDate,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = IndigoPrimary
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (visit.status) {
                                        VisitStatus.CONFIRMED -> EmeraldTrust
                                        VisitStatus.PENDING -> OrangeAccent
                                        VisitStatus.CANCELLED -> Color.Red
                                        else -> Color.Gray
                                    }
                                ) {
                                    Text(
                                        text = visit.status.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("🏠 ${visit.propertyTitle}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("👤 Tenant: ${visit.tenantName} (${visit.tenantPhone})", fontSize = 13.sp, color = TextSecondary)
                            Text("⏰ Time: ${visit.visitTime} · Visitors: ${visit.numberOfVisitors}", fontSize = 13.sp, color = TextSecondary)

                            if (visit.note.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("💬 Note: \"${visit.note}\"", fontSize = 12.sp, color = TextMuted)
                            }

                            if (visit.status == VisitStatus.PENDING) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(
                                        onClick = { onUpdateVisitStatus(visit.id, VisitStatus.CANCELLED) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Reject")
                                    }

                                    Button(
                                        onClick = { onUpdateVisitStatus(visit.id, VisitStatus.CONFIRMED) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldTrust),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Accept Visit")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
