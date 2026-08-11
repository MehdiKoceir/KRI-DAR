package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.KriDarDatabase
import com.example.data.model.*
import com.example.data.repository.KriDarRepository
import com.example.ui.navigation.*
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.admin.AdminModerationScreen
import com.example.ui.screens.chat.ChatListScreen
import com.example.ui.screens.chat.ChatRoomScreen
import com.example.ui.screens.details.PropertyDetailScreen
import com.example.ui.screens.favorites.FavoritesScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.landlord.AddPropertyScreen
import com.example.ui.screens.landlord.LandlordDashboardScreen
import com.example.ui.screens.landlord.VisitManagementScreen
import com.example.ui.screens.map.MapScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = KriDarDatabase.getInstance(applicationContext)
        val repository = KriDarRepository(db = db)

        setContent {
            KriDarTheme {
                KriDarApp(repository = repository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KriDarApp(repository: KriDarRepository) {
    val coroutineScope = rememberCoroutineScope()

    // State Collection
    val properties by repository.allProperties.collectAsState(initial = emptyList())
    val currentUser by repository.currentUser.collectAsState()
    val favoriteIds by repository.getFavoritePropertyIds().collectAsState(initial = emptyList())

    val favoriteProperties = remember(properties, favoriteIds) {
        properties.filter { favoriteIds.contains(it.id) }
    }

    val conversations by repository.getConversations().collectAsState(initial = emptyList())
    val visitRequests by repository.getVisitsForCurrentRole().collectAsState(initial = emptyList())
    val reports by repository.getAllReports().collectAsState(initial = emptyList())
    val verificationDocs by repository.getAllVerificationDocs().collectAsState(initial = emptyList())
    val wilayas = repository.wilayasList

    // Navigation State
    var currentRoute by remember { mutableStateOf(Screen.Home.route) }
    var selectedWilayaFilter by remember { mutableStateOf("Blida") }
    var selectedPropertyId by remember { mutableStateOf<String?>(null) }
    var activeConversationId by remember { mutableStateOf<String?>(null) }
    var searchQueryNav by remember { mutableStateOf("") }

    val activeNavScreens = when (currentUser.role) {
        UserRole.TENANT -> tenantBottomNavScreens
        UserRole.LANDLORD, UserRole.AGENCY -> landlordBottomNavScreens
        UserRole.ADMIN -> adminBottomNavScreens
    }

    Scaffold(
        bottomBar = {
            // Show bottom bar only on main root tabs
            val isRootTab = activeNavScreens.any { it.route == currentRoute }
            if (isRootTab && selectedPropertyId == null && activeConversationId == null) {
                Surface(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder)
                ) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 0.dp
                    ) {
                        activeNavScreens.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    currentRoute = screen.route
                                    selectedPropertyId = null
                                    activeConversationId = null
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) screen.icon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        color = if (isSelected) IndigoPrimary else TextMuted,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 10.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = IndigoPrimary,
                                    unselectedIconColor = TextMuted,
                                    indicatorColor = Indigo50
                                ),
                                modifier = Modifier.testTag("nav_item_${screen.route}")
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
        ) {
            // Detailed Sub-Screens handling
            when {
                selectedPropertyId != null -> {
                    val targetProp = properties.firstOrNull { it.id == selectedPropertyId }
                    if (targetProp != null) {
                        val priceIntel = repository.getPriceIntelligence(targetProp.wilaya, targetProp.commune, targetProp.category)

                        PropertyDetailScreen(
                            property = targetProp,
                            isFavorite = favoriteIds.contains(targetProp.id),
                            priceIntel = priceIntel,
                            onBackClick = { selectedPropertyId = null },
                            onFavoriteToggle = {
                                coroutineScope.launch {
                                    repository.toggleFavorite(targetProp.id)
                                }
                            },
                            onStartChat = {
                                coroutineScope.launch {
                                    val convId = repository.startOrCreateConversation(targetProp)
                                    activeConversationId = convId
                                    selectedPropertyId = null
                                }
                            },
                            onRequestVisit = { date, time, visitors, note ->
                                coroutineScope.launch {
                                    repository.requestVisit(
                                        property = targetProp,
                                        date = date,
                                        time = time,
                                        visitors = visitors,
                                        note = note
                                    )
                                    selectedPropertyId = null
                                    currentRoute = if (currentUser.role == UserRole.LANDLORD) Screen.LandlordVisits.route else Screen.Messages.route
                                }
                            },
                            onReportProperty = { reason, details ->
                                coroutineScope.launch {
                                    repository.submitReport(targetProp.id, targetProp.title, reason, details)
                                }
                            },
                            onCompareProperty = {
                                if (!favoriteIds.contains(targetProp.id)) {
                                    coroutineScope.launch {
                                        repository.toggleFavorite(targetProp.id)
                                    }
                                }
                                selectedPropertyId = null
                                currentRoute = Screen.Favorites.route
                            }
                        )
                    } else {
                        selectedPropertyId = null
                    }
                }

                activeConversationId != null -> {
                    val conversation = conversations.firstOrNull { it.id == activeConversationId }
                    val activeMessages by repository.getMessages(activeConversationId!!).collectAsState(initial = emptyList())

                    ChatRoomScreen(
                        conversation = conversation,
                        messages = activeMessages,
                        currentUserId = currentUser.id,
                        onBackClick = { activeConversationId = null },
                        onSendMessage = { text ->
                            coroutineScope.launch {
                                repository.sendMessage(activeConversationId!!, text)
                            }
                        }
                    )
                }

                currentRoute == "add_property" -> {
                    AddPropertyScreen(
                        currentLandlord = currentUser,
                        onBackClick = { currentRoute = Screen.LandlordDashboard.route },
                        onPropertySubmit = { newProp ->
                            coroutineScope.launch {
                                repository.addOrUpdateProperty(newProp)
                                currentRoute = Screen.LandlordDashboard.route
                            }
                        }
                    )
                }

                // Main Routes
                else -> {
                    when (currentRoute) {
                        Screen.Home.route -> {
                            HomeScreen(
                                properties = properties,
                                favoriteIds = favoriteIds,
                                wilayas = wilayas,
                                selectedWilaya = selectedWilayaFilter,
                                onWilayaChange = { selectedWilayaFilter = it },
                                onPropertyClick = { selectedPropertyId = it },
                                onFavoriteToggle = { propId ->
                                    coroutineScope.launch {
                                        repository.toggleFavorite(propId)
                                    }
                                },
                                onSearchClick = { q ->
                                    searchQueryNav = q
                                    currentRoute = Screen.Search.route
                                },
                                onMapClick = { currentRoute = Screen.Map.route }
                            )
                        }

                        Screen.Search.route -> {
                            SearchScreen(
                                properties = properties,
                                favoriteIds = favoriteIds,
                                initialQuery = searchQueryNav,
                                onPropertyClick = { selectedPropertyId = it },
                                onFavoriteToggle = { propId ->
                                    coroutineScope.launch {
                                        repository.toggleFavorite(propId)
                                    }
                                }
                            )
                        }

                        Screen.Map.route -> {
                            MapScreen(
                                properties = properties,
                                favoriteIds = favoriteIds,
                                onPropertyClick = { selectedPropertyId = it },
                                onFavoriteToggle = { propId ->
                                    coroutineScope.launch {
                                        repository.toggleFavorite(propId)
                                    }
                                }
                            )
                        }

                        Screen.Favorites.route -> {
                            FavoritesScreen(
                                favoriteProperties = favoriteProperties,
                                onPropertyClick = { selectedPropertyId = it },
                                onFavoriteToggle = { propId ->
                                    coroutineScope.launch {
                                        repository.toggleFavorite(propId)
                                    }
                                },
                                onSearchClick = { currentRoute = Screen.Search.route }
                            )
                        }

                        Screen.Messages.route -> {
                            ChatListScreen(
                                conversations = conversations,
                                currentUserId = currentUser.id,
                                onConversationClick = { activeConversationId = it }
                            )
                        }

                        Screen.LandlordDashboard.route -> {
                            LandlordDashboardScreen(
                                landlordUser = currentUser,
                                properties = properties.filter { it.landlordId == currentUser.id },
                                visits = visitRequests,
                                onAddPropertyClick = { currentRoute = "add_property" },
                                onManageVisitsClick = { currentRoute = Screen.LandlordVisits.route },
                                onPropertyClick = { selectedPropertyId = it }
                            )
                        }

                        Screen.LandlordProperties.route -> {
                            LandlordDashboardScreen(
                                landlordUser = currentUser,
                                properties = properties.filter { it.landlordId == currentUser.id },
                                visits = visitRequests,
                                onAddPropertyClick = { currentRoute = "add_property" },
                                onManageVisitsClick = { currentRoute = Screen.LandlordVisits.route },
                                onPropertyClick = { selectedPropertyId = it }
                            )
                        }

                        Screen.LandlordVisits.route -> {
                            VisitManagementScreen(
                                visits = visitRequests,
                                onUpdateVisitStatus = { visitId, status ->
                                    coroutineScope.launch {
                                        repository.updateVisitStatus(visitId, status)
                                    }
                                },
                                onBackClick = { currentRoute = Screen.LandlordDashboard.route }
                            )
                        }

                        Screen.AdminDashboard.route -> {
                            AdminDashboardScreen(
                                properties = properties,
                                reports = reports,
                                verificationDocs = verificationDocs,
                                onPropertyClick = { selectedPropertyId = it }
                            )
                        }

                        Screen.AdminModeration.route -> {
                            AdminModerationScreen(
                                reports = reports,
                                verificationDocs = verificationDocs
                            )
                        }

                        Screen.Profile.route -> {
                            ProfileScreen(
                                currentUser = currentUser,
                                onRoleSwitch = { newRole ->
                                    repository.switchUserRole(newRole)
                                    currentRoute = when (newRole) {
                                        UserRole.TENANT -> Screen.Home.route
                                        UserRole.LANDLORD, UserRole.AGENCY -> Screen.LandlordDashboard.route
                                        UserRole.ADMIN -> Screen.AdminDashboard.route
                                    }
                                },
                                onSubmitVerificationDoc = { docType, docName ->
                                    coroutineScope.launch {
                                        repository.submitVerificationDoc(docType, docName)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
