package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val unselectedIcon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Search : Screen("search", "Search", Icons.Filled.Search, Icons.Outlined.Search)
    object Map : Screen("map", "Map", Icons.Filled.LocationOn, Icons.Outlined.LocationOn)
    object Favorites : Screen("favorites", "Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
    object Messages : Screen("messages", "Messages", Icons.Filled.Chat, Icons.Outlined.Chat)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)

    // Landlord
    object LandlordDashboard : Screen("landlord_dashboard", "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    object LandlordProperties : Screen("landlord_properties", "Properties", Icons.Filled.Apartment, Icons.Outlined.Apartment)
    object LandlordVisits : Screen("landlord_visits", "Visits", Icons.Filled.Event, Icons.Outlined.Event)

    // Admin
    object AdminDashboard : Screen("admin_dashboard", "Admin", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
    object AdminModeration : Screen("admin_moderation", "Moderation", Icons.Filled.Gavel, Icons.Outlined.Gavel)
}

val tenantBottomNavScreens = listOf(
    Screen.Home,
    Screen.Search,
    Screen.Map,
    Screen.Favorites,
    Screen.Messages,
    Screen.Profile
)

val landlordBottomNavScreens = listOf(
    Screen.LandlordDashboard,
    Screen.LandlordProperties,
    Screen.LandlordVisits,
    Screen.Messages,
    Screen.Profile
)

val adminBottomNavScreens = listOf(
    Screen.AdminDashboard,
    Screen.AdminModeration,
    Screen.Messages,
    Screen.Profile
)
