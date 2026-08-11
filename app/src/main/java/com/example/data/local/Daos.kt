package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties WHERE isPaused = 0 ORDER BY createdAtTimestamp DESC")
    fun getAllProperties(): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE id = :id")
    fun getPropertyById(id: String): Flow<Property?>

    @Query("SELECT * FROM properties WHERE id = :id")
    suspend fun getPropertyDirect(id: String): Property?

    @Query("SELECT * FROM properties WHERE landlordId = :landlordId ORDER BY createdAtTimestamp DESC")
    fun getPropertiesByLandlord(landlordId: String): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE isPaused = 0 AND (wilaya LIKE '%' || :wilaya || '%' OR commune LIKE '%' || :wilaya || '%') ORDER BY createdAtTimestamp DESC")
    fun getPropertiesByWilaya(wilaya: String): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE isPaused = 0 AND category = :category ORDER BY createdAtTimestamp DESC")
    fun getPropertiesByCategory(category: PropertyCategory): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE isPaused = 0 AND priceDzd >= :minPrice AND priceDzd <= :maxPrice ORDER BY priceDzd ASC")
    fun getPropertiesByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<Property>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: Property)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperties(properties: List<Property>)

    @Update
    suspend fun updateProperty(property: Property)

    @Query("DELETE FROM properties WHERE id = :id")
    suspend fun deleteProperty(id: String)

    @Query("SELECT COUNT(*) FROM properties")
    suspend fun getPropertyCount(): Int
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<User?>

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Update
    suspend fun updateUser(user: User)
}

@Dao
interface FavoriteDao {
    @Query("SELECT propertyId FROM favorites WHERE userId = :userId")
    fun getFavoritePropertyIds(userId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE userId = :userId AND propertyId = :propertyId")
    suspend fun removeFavorite(userId: String, propertyId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND propertyId = :propertyId)")
    fun isFavorite(userId: String, propertyId: String): Flow<Boolean>
}

@Dao
interface VisitDao {
    @Query("SELECT * FROM visits WHERE tenantId = :tenantId ORDER BY requestedAt DESC")
    fun getVisitsForTenant(tenantId: String): Flow<List<VisitRequest>>

    @Query("SELECT * FROM visits WHERE landlordId = :landlordId ORDER BY requestedAt DESC")
    fun getVisitsForLandlord(landlordId: String): Flow<List<VisitRequest>>

    @Query("SELECT * FROM visits ORDER BY requestedAt DESC")
    fun getAllVisits(): Flow<List<VisitRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: VisitRequest)

    @Update
    suspend fun updateVisit(visit: VisitRequest)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM conversations WHERE tenantId = :userId OR landlordId = :userId ORDER BY updatedAtTimestamp DESC")
    fun getConversationsForUser(userId: String): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    fun getConversationById(id: String): Flow<Conversation?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestampMs ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)
}

@Dao
interface SavedSearchDao {
    @Query("SELECT * FROM saved_searches WHERE userId = :userId ORDER BY createdAt DESC")
    fun getSavedSearches(userId: String): Flow<List<SavedSearch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedSearch(search: SavedSearch)

    @Query("DELETE FROM saved_searches WHERE id = :id")
    suspend fun deleteSavedSearch(id: String)
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<Report>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: Report)

    @Update
    suspend fun updateReport(report: Report)
}

@Dao
interface VerificationDao {
    @Query("SELECT * FROM verification_docs ORDER BY submittedAt DESC")
    fun getAllVerificationDocs(): Flow<List<VerificationDoc>>

    @Query("SELECT * FROM verification_docs WHERE userId = :userId")
    fun getDocsForUser(userId: String): Flow<List<VerificationDoc>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoc(doc: VerificationDoc)

    @Update
    suspend fun updateDoc(doc: VerificationDoc)
}
