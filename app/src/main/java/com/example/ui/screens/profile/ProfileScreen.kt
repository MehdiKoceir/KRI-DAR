package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.VerificationStatus
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUser: User,
    onRoleSwitch: (UserRole) -> Unit,
    onSubmitVerificationDoc: (docType: String, docName: String) -> Unit
) {
    var showRoleDialog by remember { mutableStateOf(false) }
    var showDocDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English (EN)") }

    val languages = listOf("English (EN)", "Français (FR)", "العربية (AR)")

    Scaffold(
        topBar = {
            Surface(color = SurfaceLight, shadowElevation = 2.dp) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("User Profile & Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
            // User Profile Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(IndigoPrimary)
                        ) {
                            Text(
                                text = currentUser.fullName.take(1),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(currentUser.fullName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(currentUser.email, fontSize = 13.sp, color = TextSecondary)
                            Text("📍 ${currentUser.commune}, ${currentUser.wilaya}", fontSize = 12.sp, color = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Active User Mode:", fontSize = 11.sp, color = TextMuted)
                            Text(currentUser.role.name, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = IndigoPrimary)
                        }

                        OutlinedButton(
                            onClick = { showRoleDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("switch_role_button")
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Switch Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Verification Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldTrust)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Identity Verification", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        VerificationBadge(status = currentUser.verificationStatus)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Verified users get 3x higher response rates and trust badges on all listings.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showDocDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Upload Verification Documents (ID / Deed)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Language & App Settings
            Text("Settings & Preferences", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = OrangeAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("App Language", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }

                        Text(selectedLanguage, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = IndigoPrimary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        languages.forEach { lang ->
                            FilterChip(
                                selected = selectedLanguage == lang,
                                onClick = { selectedLanguage = lang },
                                label = { Text(lang, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Role Switcher Dialog
    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Switch User Role 🔄", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select which perspective you want to experience in Kri/Dar:", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    UserRole.values().forEach { role ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (currentUser.role == role) OrangeAccent.copy(alpha = 0.15f) else BackgroundLight
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onRoleSwitch(role)
                                    showRoleDialog = false
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = when (role) {
                                        UserRole.TENANT -> Icons.Default.Search
                                        UserRole.LANDLORD -> Icons.Default.Home
                                        UserRole.AGENCY -> Icons.Default.Apartment
                                        UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                    },
                                    contentDescription = null,
                                    tint = OrangeAccent
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(role.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = when (role) {
                                            UserRole.TENANT -> "Search & request visits for apartments"
                                            UserRole.LANDLORD -> "Post properties & manage tenant visits"
                                            UserRole.AGENCY -> "Manage multiple property portfolios"
                                            UserRole.ADMIN -> "Moderate fraud & verify landlords"
                                        },
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Doc Submission Dialog
    if (showDocDialog) {
        var docType by remember { mutableStateOf("National Identity Card (CNI)") }
        var docName by remember { mutableStateOf("cni_mehdi_front_back.pdf") }

        AlertDialog(
            onDismissRequest = { showDocDialog = false },
            title = { Text("Upload Identity Document 📄", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Identity documents are private and strictly reviewed by admins.", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = docType,
                        onValueChange = { docType = it },
                        label = { Text("Document Type") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = docName,
                        onValueChange = { docName = it },
                        label = { Text("File Attachment Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmitVerificationDoc(docType, docName)
                        showDocDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldTrust)
                ) {
                    Text("Submit for Review", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDocDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
