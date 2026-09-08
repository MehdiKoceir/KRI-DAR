package com.example.ui.screens.details

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PriceIntelligence
import com.example.data.model.Property
import com.example.data.model.PropertyReview
import com.example.data.model.VerificationStatus
import com.example.ui.components.VerificationBadge
import com.example.ui.components.formatDzd
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailScreen(
    property: Property,
    isFavorite: Boolean,
    priceIntel: PriceIntelligence,
    reviews: List<PropertyReview> = emptyList(),
    onBackClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onStartChat: () -> Unit,
    onRequestVisit: (date: String, time: String, visitors: Int, note: String) -> Unit,
    onReportProperty: (reason: String, details: String) -> Unit,
    onCompareProperty: () -> Unit,
    onAddReview: (rating: Int, comment: String, rentalPeriod: String, cleanliness: Int, communication: Int, accuracy: Int, location: Int) -> Unit = { _, _, _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    var selectedImageIndex by remember { mutableStateOf(0) }
    var showVisitDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showContactOptionsSheet by remember { mutableStateOf(false) }
    var showAddReviewSheet by remember { mutableStateOf(false) }

    val images = property.imageResNames.ifEmpty { listOf("img_property_algiers_f3_1786376194767") }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showContactOptionsSheet = true },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Contact Owner",
                        tint = Color.White
                    )
                },
                text = {
                    Text(
                        text = "Contacter",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                containerColor = OrangeAccent,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("fab_contact_owner")
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = {
            Surface(
                color = SurfaceLight,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onStartChat,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IndigoPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("contact_landlord_button")
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Direct Chat", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = { showVisitDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("request_visit_button")
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Request Visit", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
        ) {
            // Hero Photo Gallery Slider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                val currentImgName = images.getOrNull(selectedImageIndex) ?: ""
                val resId = context.resources.getIdentifier(currentImgName, "drawable", context.packageName)

                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = property.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(IndigoPrimaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apartment,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                // Top Navigation overlay
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceLight.copy(alpha = 0.9f))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }

                    Row {
                        IconButton(
                            onClick = onCompareProperty,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SurfaceLight.copy(alpha = 0.9f))
                        ) {
                            Icon(Icons.Default.Compare, contentDescription = "Compare", tint = IndigoPrimary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onFavoriteToggle,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SurfaceLight.copy(alpha = 0.9f))
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color.Red else TextSecondary
                            )
                        }
                    }
                }

                // Photo Indicators
                if (images.size > 1) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    ) {
                        images.forEachIndexed { idx, _ ->
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(if (idx == selectedImageIndex) 10.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(if (idx == selectedImageIndex) OrangeAccent else Color.White.copy(alpha = 0.7f))
                            )
                        }
                    }
                }
            }

            // Thumbnail Gallery Row
            if (images.size > 1) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(images.size) { idx ->
                        val imgName = images[idx]
                        val rId = context.resources.getIdentifier(imgName, "drawable", context.packageName)
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (idx == selectedImageIndex) 2.dp else 0.dp,
                                    color = if (idx == selectedImageIndex) OrangeAccent else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedImageIndex = idx }
                        ) {
                            if (rId != 0) {
                                Image(
                                    painter = painterResource(id = rId),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            // Property Key Content Block
            Card(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-12).dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Category & Verification Badges
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = IndigoPrimaryContainer.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${property.category.displayName} · ${property.rentalType.name}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        VerificationBadge(
                            status = property.landlordVerification,
                            text = "Verified Property"
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = property.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = OrangeAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${property.commune}, ${property.wilaya} (${property.neighborhood})",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Price Header
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = OrangeAccent.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = formatDzd(property.priceDzd),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OrangeAccent
                                )
                                Text(
                                    text = "Monthly Rent",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Deposit: ${property.depositMonths} months",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Available: ${property.availableFrom}",
                                    fontSize = 12.sp,
                                    color = EmeraldTrust,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Rental Price Intelligence Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Market Rent Intelligence 💡",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoPrimary
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldLight
                                ) {
                                    Text(
                                        text = "🟢 Fair Price",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldTrust,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Average ${property.category.name} in ${property.commune}: ${formatDzd(priceIntel.averagePriceDzd)}",
                                fontSize = 13.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Typical local range: ${formatDzd(priceIntel.minPriceDzd)} – ${formatDzd(priceIntel.maxPriceDzd)}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Key Specs Grid
                    Text("Property Specifications", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SpecBox(icon = Icons.Default.SquareFoot, label = "Area", value = "${property.surfaceM2} m²")
                        SpecBox(icon = Icons.Default.MeetingRoom, label = "Rooms", value = "${property.totalRooms} rooms")
                        SpecBox(icon = Icons.Default.Bed, label = "Bedrooms", value = "${property.bedrooms}")
                        SpecBox(icon = Icons.Default.Bathtub, label = "Baths", value = "${property.bathrooms}")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Amenities
                    Text("Amenities & Features", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            AmenityCheck(label = "Furnished", isCheck = property.isFurnished)
                            AmenityCheck(label = "Parking", isCheck = property.hasParking)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            AmenityCheck(label = "Elevator", isCheck = property.hasElevator)
                            AmenityCheck(label = "Balcony", isCheck = property.hasBalcony)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            AmenityCheck(label = "Central Heating", isCheck = property.hasHeating)
                            AmenityCheck(label = "Air Conditioning", isCheck = property.hasAc)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            AmenityCheck(label = "High-speed Internet", isCheck = property.hasInternet)
                            AmenityCheck(label = "Pets Allowed", isCheck = property.allowsPets)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Description
                    Text("Description", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = property.description,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Landlord Info Card
                    Text("Listed by", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(IndigoPrimary)
                            ) {
                                Text(
                                    text = property.landlordName.take(1),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = property.landlordName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Landlord",
                                        tint = VerificationGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    text = "Propriétaire vérifié · ${property.landlordPhone}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "⚡ Répond généralement en 15 min",
                                    fontSize = 11.sp,
                                    color = EmeraldTrust,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Quick Contact Action Buttons
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        try {
                                            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:${property.landlordPhone.replace(" ", "")}")
                                            }
                                            context.startActivity(dialIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Impossible d'ouvrir le composeur", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Indigo100)
                                        .testTag("landlord_quick_call_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Appeler",
                                        tint = IndigoPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = {
                                        try {
                                            val cleanPhone = property.landlordPhone
                                                .replace("+", "")
                                                .replace(" ", "")
                                                .replace("-", "")
                                            val inquiry = "Bonjour ${property.landlordName}, je vous contacte concernant votre annonce : \"${property.title}\" sur Kri/Dar."
                                            val encoded = URLEncoder.encode(inquiry, "UTF-8")
                                            val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encoded"))
                                            context.startActivity(waIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "WhatsApp non disponible", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDCFCE7))
                                        .testTag("landlord_quick_whatsapp_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = "WhatsApp",
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Report button
                    TextButton(
                        onClick = { showReportDialog = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Report suspicious listing or fraud", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }

    // Request Visit Dialog
    if (showVisitDialog) {
        var visitDate by remember { mutableStateOf("Saturday, 15 August 2026") }
        var visitTime by remember { mutableStateOf("15:00") }
        var visitorsCount by remember { mutableStateOf("2") }
        var noteText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showVisitDialog = false },
            title = { Text("Request Property Visit 📅", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select your preferred visit details:", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = visitDate,
                        onValueChange = { visitDate = it },
                        label = { Text("Preferred Date") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = visitTime,
                        onValueChange = { visitTime = it },
                        label = { Text("Preferred Time (e.g. 15:00)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Message to Landlord (Optional)") },
                        placeholder = { Text("e.g. Family visit for 1 year contract") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRequestVisit(visitDate, visitTime, visitorsCount.toIntOrNull() ?: 1, noteText)
                        showVisitDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) {
                    Text("Send Visit Request", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showVisitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Report Property Dialog
    if (showReportDialog) {
        var reportReason by remember { mutableStateOf("Fake property") }
        var reportDetails by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Listing 🚨", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Why are you reporting this property?", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    val reasons = listOf("Fake property", "Wrong price", "Already rented", "Scam attempt", "Fake photos")
                    reasons.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reportReason = r }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = reportReason == r, onClick = { reportReason = r })
                            Text(r, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reportDetails,
                        onValueChange = { reportDetails = it },
                        label = { Text("Additional Details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReportProperty(reportReason, reportDetails)
                        showReportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Submit Report", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Direct Contact Modal Bottom Sheet
    if (showContactOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showContactOptionsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = SurfaceLight,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.testTag("contact_options_bottom_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 32.dp, top = 8.dp)
            ) {
                // Landlord Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimaryContainer)
                    ) {
                        Text(
                            text = property.landlordName.take(1),
                            color = IndigoPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = property.landlordName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = VerificationGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Propriétaire vérifié · ${property.landlordPhone}",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "⚡ Répond généralement en 15 min",
                            fontSize = 11.sp,
                            color = VerificationGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(onClick = { showContactOptionsSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = OutlineBorder)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Choisir le mode de contact direct :",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Option 1: Appeler le propriétaire (Phone Call)
                Surface(
                    onClick = {
                        try {
                            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${property.landlordPhone.replace(" ", "")}")
                            }
                            context.startActivity(dialIntent)
                            showContactOptionsSheet = false
                        } catch (e: Exception) {
                            Toast.makeText(context, "Impossible d'ouvrir le composeur téléphonique", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = Indigo100.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_action_call")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(IndigoPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Appel Téléphonique Direct",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${property.landlordPhone} · Ouvre l'application Téléphone",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option 2: Message WhatsApp Direct
                Surface(
                    onClick = {
                        try {
                            val cleanPhone = property.landlordPhone
                                .replace("+", "")
                                .replace(" ", "")
                                .replace("-", "")
                            val inquiryMessage = "Bonjour ${property.landlordName}, je vous contacte depuis l'application Kri/Dar concernant votre annonce : \"${property.title}\" (${formatDzd(property.priceDzd)}/mois à ${property.commune}). Est-elle toujours disponible ?"
                            val encodedMsg = URLEncoder.encode(inquiryMessage, "UTF-8")
                            val whatsappUri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg")
                            val waIntent = Intent(Intent.ACTION_VIEW, whatsappUri)
                            context.startActivity(waIntent)
                            showContactOptionsSheet = false
                        } catch (e: Exception) {
                            Toast.makeText(context, "WhatsApp n'est pas installé sur cet appareil", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFDCFCE7),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_action_whatsapp")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16A34A))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Message WhatsApp Direct",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF14532D)
                            )
                            Text(
                                text = "Ouvre WhatsApp avec message pré-rempli",
                                fontSize = 12.sp,
                                color = Color(0xFF166534)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFF16A34A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option 3: SMS Direct
                Surface(
                    onClick = {
                        try {
                            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("smsto:${property.landlordPhone.replace(" ", "")}")
                                putExtra("sms_body", "Bonjour ${property.landlordName}, je vous contacte via Kri/Dar au sujet de votre location : ${property.title}.")
                            }
                            context.startActivity(smsIntent)
                            showContactOptionsSheet = false
                        } catch (e: Exception) {
                            Toast.makeText(context, "Impossible d'ouvrir l'application SMS", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = BackgroundLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_action_sms")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Envoyer un SMS",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Messagerie mobile standard",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option 4: Chat In-App Kri/Dar
                Surface(
                    onClick = {
                        showContactOptionsSheet = false
                        onStartChat()
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = BackgroundLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_action_inapp_chat")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Indigo100)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Chat Sécurisé Kri/Dar",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Historique et documents conservés dans l'app",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecBox(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BackgroundLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
            Text(label, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Composable
private fun AmenityCheck(label: String, isCheck: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isCheck) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (isCheck) VerificationGreen else TextMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (isCheck) TextPrimary else TextMuted,
            fontWeight = if (isCheck) FontWeight.Medium else FontWeight.Normal
        )
    }
}
