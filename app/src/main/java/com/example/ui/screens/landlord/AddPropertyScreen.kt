package com.example.ui.screens.landlord

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPropertyScreen(
    currentLandlord: User,
    onBackClick: () -> Unit,
    onPropertySubmit: (Property) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("85000") }
    var category by remember { mutableStateOf(PropertyCategory.APARTMENT) }
    var rentalType by remember { mutableStateOf(RentalType.MONTHLY) }
    var wilaya by remember { mutableStateOf("Blida") }
    var commune by remember { mutableStateOf("Ouled Yaich") }
    var surfaceInput by remember { mutableStateOf("95") }
    var bedroomsInput by remember { mutableStateOf("2") }
    var bathroomsInput by remember { mutableStateOf("2") }
    var isFurnished by remember { mutableStateOf(true) }
    var hasParking by remember { mutableStateOf(true) }
    var hasHeating by remember { mutableStateOf(true) }
    var hasAc by remember { mutableStateOf(true) }
    var isFamilyOnly by remember { mutableStateOf(true) }

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
                    Text("Post New Property 🏠", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                .padding(20.dp)
        ) {
            Text("General Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Listing Title") },
                placeholder = { Text("e.g. Modern F3 Apartment in High Standing Residence") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("property_title_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Detailed Description") },
                placeholder = { Text("Describe rooms, orientation, building quality, surroundings...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Price & Conditions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = priceInput,
                onValueChange = { priceInput = it },
                label = { Text("Monthly Rent (DZD)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("property_price_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Location", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = wilaya,
                    onValueChange = { wilaya = it },
                    label = { Text("Wilaya") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = commune,
                    onValueChange = { commune = it },
                    label = { Text("Commune") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Specifications", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = surfaceInput,
                    onValueChange = { surfaceInput = it },
                    label = { Text("Surface m²") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bedroomsInput,
                    onValueChange = { bedroomsInput = it },
                    label = { Text("Bedrooms") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bathroomsInput,
                    onValueChange = { bathroomsInput = it },
                    label = { Text("Bathrooms") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Amenities", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isFurnished, onCheckedChange = { isFurnished = it }, colors = CheckboxDefaults.colors(checkedColor = OrangeAccent))
                Text("Furnished")
                Spacer(modifier = Modifier.width(16.dp))
                Checkbox(checked = hasParking, onCheckedChange = { hasParking = it }, colors = CheckboxDefaults.colors(checkedColor = OrangeAccent))
                Text("Garage Parking")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = hasHeating, onCheckedChange = { hasHeating = it }, colors = CheckboxDefaults.colors(checkedColor = OrangeAccent))
                Text("Heating")
                Spacer(modifier = Modifier.width(16.dp))
                Checkbox(checked = hasAc, onCheckedChange = { hasAc = it }, colors = CheckboxDefaults.colors(checkedColor = OrangeAccent))
                Text("Air Conditioning")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val newProp = Property(
                        id = UUID.randomUUID().toString(),
                        title = title.ifBlank { "Modern Rental Property" },
                        description = description.ifBlank { "Beautiful property available for rent." },
                        priceDzd = priceInput.toDoubleOrNull() ?: 80000.0,
                        category = category,
                        rentalType = rentalType,
                        wilaya = wilaya.ifBlank { "Blida" },
                        commune = commune.ifBlank { "Ouled Yaich" },
                        latitude = 36.4950,
                        longitude = 2.8520,
                        surfaceM2 = surfaceInput.toIntOrNull() ?: 90,
                        bedrooms = bedroomsInput.toIntOrNull() ?: 2,
                        bathrooms = bathroomsInput.toIntOrNull() ?: 1,
                        totalRooms = (bedroomsInput.toIntOrNull() ?: 2) + 1,
                        imageResNames = listOf("img_property_algiers_f3_1786376194767", "img_property_bab_ezzouar_f2_1786376266050"),
                        isFurnished = isFurnished,
                        hasParking = hasParking,
                        hasHeating = hasHeating,
                        hasAc = hasAc,
                        isFamilyOnly = isFamilyOnly,
                        landlordId = currentLandlord.id,
                        landlordName = currentLandlord.fullName,
                        landlordVerification = currentLandlord.verificationStatus,
                        isVerifiedProperty = true
                    )
                    onPropertySubmit(newProp)
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_property_button")
            ) {
                Text("Publish Listing on Kri/Dar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
