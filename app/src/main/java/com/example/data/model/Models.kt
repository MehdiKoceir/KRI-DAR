package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

enum class UserRole {
    TENANT, LANDLORD, AGENCY, ADMIN
}

enum class PropertyCategory {
    APARTMENT, HOUSE, VILLA, STUDIO, ROOM, DUPLEX, OFFICE, COMMERCIAL, OTHER
}

enum class RentalType {
    MONTHLY, YEARLY, SHORT_TERM, STUDENT, FAMILY, ROOM, SHARED, COMMERCIAL
}

enum class VerificationStatus {
    UNVERIFIED, PENDING, VERIFIED, REJECTED, SUSPENDED
}

enum class VisitStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED, SUGGEST_NEW_TIME
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val avatarUrl: String = "",
    val wilaya: String = "Blida",
    val commune: String = "Ouled Yaich",
    val verificationStatus: VerificationStatus = VerificationStatus.UNVERIFIED,
    val isAgency: Boolean = false,
    val agencyName: String? = null,
    val memberSince: String = "2025",
    val responseRate: Int = 96, // percentage
    val responseTimeText: String = "15 min"
) : Serializable

@Entity(tableName = "wilayas")
data class Wilaya(
    @PrimaryKey val code: String, // e.g. "09"
    val nameFr: String,          // e.g. "Blida"
    val nameAr: String,          // e.g. "البليدة"
    val communes: List<String>
) : Serializable

@Entity(tableName = "properties")
data class Property(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val priceDzd: Double,        // Rent per month or period
    val category: PropertyCategory,
    val rentalType: RentalType,
    val wilaya: String,
    val commune: String,
    val neighborhood: String = "",
    val latitude: Double,
    val longitude: Double,
    val surfaceM2: Int,
    val bedrooms: Int,
    val bathrooms: Int,
    val totalRooms: Int,
    val floorLevel: Int = 1,
    val imageResNames: List<String>, // drawable resource names or URLs
    val isFurnished: Boolean = true,
    val hasParking: Boolean = true,
    val hasElevator: Boolean = false,
    val hasBalcony: Boolean = true,
    val hasHeating: Boolean = true,
    val hasAc: Boolean = true,
    val hasInternet: Boolean = true,
    val allowsPets: Boolean = false,
    val isFamilyOnly: Boolean = false,
    val isStudentFriendly: Boolean = true,
    val landlordId: String,
    val landlordName: String,
    val landlordAvatar: String = "",
    val landlordVerification: VerificationStatus = VerificationStatus.VERIFIED,
    val isVerifiedProperty: Boolean = true,
    val availableFrom: String = "Immediate",
    val depositMonths: Int = 2,
    val agencyFeeDzd: Double = 0.0,
    val viewsCount: Int = 1240,
    val isRented: Boolean = false,
    val isPaused: Boolean = false,
    val isFeatured: Boolean = false,
    val isFlaggedSuspicious: Boolean = false,
    val createdAtTimestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val propertyId: String,
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "visits")
data class VisitRequest(
    @PrimaryKey val id: String,
    val propertyId: String,
    val propertyTitle: String,
    val propertyImageRes: String,
    val propertyLocation: String,
    val tenantId: String,
    val tenantName: String,
    val tenantPhone: String,
    val landlordId: String,
    val visitDate: String,    // e.g. "Saturday, 15 August 2026"
    val visitTime: String,    // e.g. "15:00"
    val numberOfVisitors: Int = 1,
    val note: String = "",
    val status: VisitStatus = VisitStatus.PENDING,
    val requestedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String,
    val propertyId: String,
    val propertyTitle: String,
    val propertyPriceDzd: Double,
    val propertyLocation: String,
    val propertyImageRes: String,
    val tenantId: String,
    val tenantName: String,
    val landlordId: String,
    val landlordName: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0,
    val updatedAtTimestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val imageUrl: String? = null,
    val isRead: Boolean = false,
    val timestampText: String = "10:30 AM",
    val timestampMs: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "saved_searches")
data class SavedSearch(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val wilaya: String?,
    val commune: String?,
    val category: PropertyCategory?,
    val maxPriceDzd: Double?,
    val minBedrooms: Int?,
    val alertsEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey val id: String,
    val propertyId: String,
    val propertyTitle: String,
    val reporterUserId: String,
    val reason: String, // e.g. "Fake property", "Wrong price", "Scam attempt"
    val details: String,
    val status: String = "PENDING", // PENDING, RESOLVED, DISMISSED
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "verification_docs")
data class VerificationDoc(
    @PrimaryKey val id: String,
    val userId: String,
    val docType: String, // "National ID", "Passport", "Property Title Deed"
    val docName: String,
    val status: VerificationStatus = VerificationStatus.PENDING,
    val submittedAt: Long = System.currentTimeMillis()
) : Serializable

data class PriceIntelligence(
    val wilaya: String,
    val commune: String,
    val category: PropertyCategory,
    val averagePriceDzd: Double,
    val minPriceDzd: Double,
    val maxPriceDzd: Double,
    val averagePricePerM2: Double,
    val priceTrendPercent: Double // e.g. +3.5%
)

data class PropertyFilter(
    val query: String = "",
    val wilaya: String? = null,
    val commune: String? = null,
    val minPriceDzd: Double? = null,
    val maxPriceDzd: Double? = null,
    val category: PropertyCategory? = null,
    val rentalType: RentalType? = null,
    val minBedrooms: Int? = null,
    val isFurnished: Boolean? = null,
    val isVerifiedOnly: Boolean? = null,
    val isFamilyOnly: Boolean? = null,
    val isStudentFriendly: Boolean? = null
)

