package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Property
import com.example.data.model.Report
import com.example.data.model.VerificationDoc
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatCard
import com.example.ui.components.formatDzd
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    properties: List<Property>,
    reports: List<Report>,
    verificationDocs: List<VerificationDoc>,
    onPropertyClick: (String) -> Unit
) {
    val suspiciousListings = properties.filter { it.isFlaggedSuspicious }

    Scaffold(
        topBar = {
            Surface(color = SurfaceLight, shadowElevation = 2.dp) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Kri/Dar Admin Control 🇩🇿", fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
            Text("Platform Statistics", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Total Listings", "${properties.size}", Icons.Default.Apartment, IndigoPrimary, Modifier.weight(1f))
                StatCard("Anti-Scam Flags", "${suspiciousListings.size}", Icons.Default.Warning, Color.Red, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("User Reports", "${reports.size}", Icons.Default.Flag, OrangeAccent, Modifier.weight(1f))
                StatCard("Pending ID Docs", "${verificationDocs.size}", Icons.Default.VerifiedUser, EmeraldTrust, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Anti-Scam Price Anomaly Queue 🚨", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (suspiciousListings.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("No suspicious price anomalies detected.", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(16.dp))
                }
            } else {
                suspiciousListings.forEach { prop ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("FLAGGED: Price Anomaly", fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C), fontSize = 13.sp)
                                Text(formatDzd(prop.priceDzd), fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(prop.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("📍 ${prop.commune}, ${prop.wilaya} · Landlord: ${prop.landlordName}", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminModerationScreen(
    reports: List<Report>,
    verificationDocs: List<VerificationDoc>
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
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = OrangeAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Moderation & Verification Queue", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundLight)
        ) {
            item {
                Text("Tenant & Fraud Reports (${reports.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            if (reports.isEmpty()) {
                item {
                    Text("No user reports pending review.", fontSize = 13.sp, color = TextMuted)
                }
            } else {
                items(reports) { r ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("🚨 Reason: ${r.reason}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Red)
                                Text(r.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)
                            }
                            Text("Property: ${r.propertyTitle}", fontSize = 13.sp, color = TextPrimary)
                            if (r.details.isNotEmpty()) {
                                Text("Details: ${r.details}", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Landlord Verification Documents (${verificationDocs.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            if (verificationDocs.isEmpty()) {
                item {
                    Text("No document verifications pending.", fontSize = 13.sp, color = TextMuted)
                }
            } else {
                items(verificationDocs) { doc ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("📄 Doc Type: ${doc.docType}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Document Name: ${doc.docName}", fontSize = 13.sp, color = TextSecondary)
                            Text("Status: ${doc.status}", fontSize = 12.sp, color = EmeraldTrust, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
