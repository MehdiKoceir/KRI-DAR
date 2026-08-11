package com.example.data.repository

import com.example.data.local.KriDarDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class KriDarRepository(private val db: KriDarDatabase) {

    private val scope = CoroutineScope(Dispatchers.IO)

    // Current active user
    private val _currentUser = MutableStateFlow(
        User(
            id = "tenant_mehdi",
            fullName = "Mehdi K.",
            email = "mehdi@kridar.dz",
            phone = "+213 550 12 34 56",
            role = UserRole.TENANT,
            wilaya = "Blida",
            commune = "Ouled Yaich",
            verificationStatus = VerificationStatus.VERIFIED,
            memberSince = "2025"
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    init {
        scope.launch {
            if (db.propertyDao().getPropertyCount() == 0) {
                seedDatabase()
            }
        }
    }

    fun switchUserRole(newRole: UserRole) {
        val current = _currentUser.value
        val updated = when (newRole) {
            UserRole.TENANT -> current.copy(
                id = "tenant_mehdi",
                fullName = "Mehdi K. (Tenant)",
                role = UserRole.TENANT,
                isAgency = false
            )
            UserRole.LANDLORD -> current.copy(
                id = "landlord_ahmed",
                fullName = "Ahmed B. (Landlord)",
                role = UserRole.LANDLORD,
                verificationStatus = VerificationStatus.VERIFIED,
                isAgency = false,
                responseRate = 96,
                responseTimeText = "15 min"
            )
            UserRole.AGENCY -> current.copy(
                id = "agency_immo",
                fullName = "Blida Immobilier Agency",
                role = UserRole.AGENCY,
                isAgency = true,
                agencyName = "Sarl Blida Immo",
                verificationStatus = VerificationStatus.VERIFIED
            )
            UserRole.ADMIN -> current.copy(
                id = "admin_kridar",
                fullName = "Kri/Dar Support Team",
                role = UserRole.ADMIN,
                verificationStatus = VerificationStatus.VERIFIED
            )
        }
        _currentUser.value = updated
    }

    // --- Properties Flow ---
    val allProperties: Flow<List<Property>> = db.propertyDao().getAllProperties()

    fun getPropertyById(id: String): Flow<Property?> = db.propertyDao().getPropertyById(id)

    fun getPropertiesForLandlord(landlordId: String): Flow<List<Property>> =
        db.propertyDao().getPropertiesByLandlord(landlordId)

    /**
     * Fetch properties filtered by location (Wilaya or Commune name).
     */
    fun getPropertiesByLocation(wilaya: String): Flow<List<Property>> =
        db.propertyDao().getPropertiesByWilaya(wilaya)

    /**
     * Fetch properties filtered by category/type (e.g. APARTMENT, VILLA, HOUSE).
     */
    fun getPropertiesByCategory(category: PropertyCategory): Flow<List<Property>> =
        db.propertyDao().getPropertiesByCategory(category)

    /**
     * Fetch properties within a specific price range in DZD.
     */
    fun getPropertiesByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<Property>> =
        db.propertyDao().getPropertiesByPriceRange(minPrice, maxPrice)

    /**
     * Search and filter properties using a structured [PropertyFilter] object.
     * Supports multi-criteria filtering: search query, location (wilaya/commune),
     * price range, property category, rental type, bedrooms, furnished, verified only, etc.
     */
    fun searchProperties(filter: PropertyFilter): Flow<List<Property>> {
        return allProperties.map { list ->
            list.filter { prop ->
                val matchQuery = filter.query.isBlank() ||
                        prop.title.contains(filter.query, ignoreCase = true) ||
                        prop.description.contains(filter.query, ignoreCase = true) ||
                        prop.wilaya.contains(filter.query, ignoreCase = true) ||
                        prop.commune.contains(filter.query, ignoreCase = true) ||
                        prop.neighborhood.contains(filter.query, ignoreCase = true)

                val matchWilaya = filter.wilaya.isNullOrBlank() || filter.wilaya.equals("All", ignoreCase = true) || prop.wilaya.equals(filter.wilaya, ignoreCase = true)
                val matchCommune = filter.commune.isNullOrBlank() || filter.commune.equals("All", ignoreCase = true) || prop.commune.equals(filter.commune, ignoreCase = true)
                val matchMinPrice = filter.minPriceDzd == null || prop.priceDzd >= filter.minPriceDzd
                val matchMaxPrice = filter.maxPriceDzd == null || prop.priceDzd <= filter.maxPriceDzd
                val matchCategory = filter.category == null || prop.category == filter.category
                val matchRentalType = filter.rentalType == null || prop.rentalType == filter.rentalType
                val matchBedrooms = filter.minBedrooms == null || prop.bedrooms >= filter.minBedrooms
                val matchFurnished = filter.isFurnished == null || !filter.isFurnished || prop.isFurnished
                val matchVerified = filter.isVerifiedOnly == null || !filter.isVerifiedOnly || prop.isVerifiedProperty
                val matchFamily = filter.isFamilyOnly == null || !filter.isFamilyOnly || prop.isFamilyOnly
                val matchStudent = filter.isStudentFriendly == null || !filter.isStudentFriendly || prop.isStudentFriendly

                matchQuery && matchWilaya && matchCommune && matchMinPrice && matchMaxPrice &&
                        matchCategory && matchRentalType && matchBedrooms && matchFurnished &&
                        matchVerified && matchFamily && matchStudent
            }
        }
    }

    /**
     * Convenience search method accepting individual search parameters.
     */
    fun searchProperties(
        query: String = "",
        wilaya: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        category: PropertyCategory? = null
    ): Flow<List<Property>> {
        return searchProperties(
            PropertyFilter(
                query = query,
                wilaya = wilaya,
                minPriceDzd = minPrice,
                maxPriceDzd = maxPrice,
                category = category
            )
        )
    }

    // --- Favorites Flow ---
    fun getFavoritePropertyIds(): Flow<List<String>> =
        _currentUser.flatMapLatest { user -> db.favoriteDao().getFavoritePropertyIds(user.id) }

    suspend fun toggleFavorite(propertyId: String) {
        val userId = _currentUser.value.id
        val isFav = db.favoriteDao().isFavorite(userId, propertyId).first()
        if (isFav) {
            db.favoriteDao().removeFavorite(userId, propertyId)
        } else {
            db.favoriteDao().addFavorite(Favorite(userId = userId, propertyId = propertyId))
        }
    }

    // --- Visits Flow ---
    fun getVisitsForCurrentRole(): Flow<List<VisitRequest>> {
        return _currentUser.flatMapLatest { user ->
            when (user.role) {
                UserRole.TENANT -> db.visitDao().getVisitsForTenant(user.id)
                UserRole.LANDLORD, UserRole.AGENCY -> db.visitDao().getVisitsForLandlord(user.id)
                UserRole.ADMIN -> db.visitDao().getAllVisits()
            }
        }
    }

    suspend fun requestVisit(
        property: Property,
        date: String,
        time: String,
        visitors: Int,
        note: String
    ) {
        val user = _currentUser.value
        val visit = VisitRequest(
            id = UUID.randomUUID().toString(),
            propertyId = property.id,
            propertyTitle = property.title,
            propertyImageRes = property.imageResNames.firstOrNull() ?: "",
            propertyLocation = "${property.commune}, ${property.wilaya}",
            tenantId = user.id,
            tenantName = user.fullName,
            tenantPhone = user.phone,
            landlordId = property.landlordId,
            visitDate = date,
            visitTime = time,
            numberOfVisitors = visitors,
            note = note,
            status = VisitStatus.PENDING
        )
        db.visitDao().insertVisit(visit)
    }

    suspend fun updateVisitStatus(visitId: String, newStatus: VisitStatus) {
        val visits = db.visitDao().getAllVisits().first()
        val visit = visits.find { it.id == visitId } ?: return
        db.visitDao().updateVisit(visit.copy(status = newStatus))
    }

    // --- Conversations & Messages ---
    fun getConversations(): Flow<List<Conversation>> =
        _currentUser.flatMapLatest { user -> db.messageDao().getConversationsForUser(user.id) }

    fun getMessages(conversationId: String): Flow<List<Message>> =
        db.messageDao().getMessagesForConversation(conversationId)

    suspend fun startOrCreateConversation(property: Property): String {
        val user = _currentUser.value
        val convId = "conv_${user.id}_${property.landlordId}_${property.id}"
        val existing = db.messageDao().getConversationsForUser(user.id).first().find { it.id == convId }
        if (existing == null) {
            val conv = Conversation(
                id = convId,
                propertyId = property.id,
                propertyTitle = property.title,
                propertyPriceDzd = property.priceDzd,
                propertyLocation = "${property.commune}, ${property.wilaya}",
                propertyImageRes = property.imageResNames.firstOrNull() ?: "",
                tenantId = user.id,
                tenantName = user.fullName,
                landlordId = property.landlordId,
                landlordName = property.landlordName,
                lastMessage = "Salam Alaykoum, is this property still available?",
                lastMessageTime = "Just now",
                unreadCount = 0
            )
            db.messageDao().insertConversation(conv)
            // Add initial message
            val msg = Message(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                senderId = user.id,
                senderName = user.fullName,
                text = "Salam Alaykoum! I am interested in your property ${property.title} in ${property.commune}. Is it available for a visit?",
                timestampText = "Just now"
            )
            db.messageDao().insertMessage(msg)
        }
        return convId
    }

    suspend fun sendMessage(conversationId: String, text: String) {
        val user = _currentUser.value
        val msg = Message(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = user.id,
            senderName = user.fullName,
            text = text,
            timestampText = "Just now"
        )
        db.messageDao().insertMessage(msg)
    }

    // --- Property Management (Landlord) ---
    suspend fun addOrUpdateProperty(property: Property) {
        // Anti-scam price anomaly check
        val avgPrice = getPriceIntelligence(property.wilaya, property.commune, property.category).averagePriceDzd
        val isFlagged = if (avgPrice > 0) (property.priceDzd < avgPrice * 0.35) else false
        val finalProperty = property.copy(
            isFlaggedSuspicious = isFlagged,
            isVerifiedProperty = if (isFlagged) false else property.isVerifiedProperty
        )
        db.propertyDao().insertProperty(finalProperty)
    }

    suspend fun markPropertyRented(propertyId: String, isRented: Boolean) {
        val prop = db.propertyDao().getPropertyDirect(propertyId) ?: return
        db.propertyDao().updateProperty(prop.copy(isRented = isRented))
    }

    suspend fun togglePauseProperty(propertyId: String) {
        val prop = db.propertyDao().getPropertyDirect(propertyId) ?: return
        db.propertyDao().updateProperty(prop.copy(isPaused = !prop.isPaused))
    }

    suspend fun deleteProperty(propertyId: String) {
        db.propertyDao().deleteProperty(propertyId)
    }

    // --- Reports & Anti-Scam ---
    fun getAllReports(): Flow<List<Report>> = db.reportDao().getAllReports()

    suspend fun submitReport(propertyId: String, propertyTitle: String, reason: String, details: String) {
        val user = _currentUser.value
        val report = Report(
            id = UUID.randomUUID().toString(),
            propertyId = propertyId,
            propertyTitle = propertyTitle,
            reporterUserId = user.id,
            reason = reason,
            details = details,
            status = "PENDING"
        )
        db.reportDao().insertReport(report)
    }

    // --- Verification Docs ---
    fun getAllVerificationDocs(): Flow<List<VerificationDoc>> = db.verificationDao().getAllVerificationDocs()

    suspend fun submitVerificationDoc(docType: String, docName: String) {
        val user = _currentUser.value
        val doc = VerificationDoc(
            id = UUID.randomUUID().toString(),
            userId = user.id,
            docType = docType,
            docName = docName,
            status = VerificationStatus.PENDING
        )
        db.verificationDao().insertDoc(doc)
    }

    suspend fun updateDocStatus(docId: String, status: VerificationStatus) {
        val docs = db.verificationDao().getAllVerificationDocs().first()
        val doc = docs.find { it.id == docId } ?: return
        db.verificationDao().updateDoc(doc.copy(status = status))
    }

    // --- Price Intelligence Calculator ---
    fun getPriceIntelligence(wilaya: String, commune: String, category: PropertyCategory): PriceIntelligence {
        val baseAvg = when (wilaya.lowercase()) {
            "alger", "algiers" -> 110000.0
            "blida" -> 82000.0
            "oran" -> 85000.0
            "sétif", "setif" -> 65000.0
            "constantine" -> 70000.0
            "annaba" -> 75000.0
            "tizi ouzou" -> 60000.0
            else -> 55000.0
        }
        val categoryMultiplier = when (category) {
            PropertyCategory.VILLA -> 2.2
            PropertyCategory.DUPLEX -> 1.5
            PropertyCategory.APARTMENT, PropertyCategory.HOUSE -> 1.0
            PropertyCategory.STUDIO -> 0.55
            PropertyCategory.ROOM -> 0.35
            PropertyCategory.COMMERCIAL, PropertyCategory.OFFICE -> 1.8
            PropertyCategory.OTHER -> 0.8
        }
        val avg = baseAvg * categoryMultiplier
        val min = avg * 0.8
        val max = avg * 1.25
        val avgM2 = avg / 90.0

        return PriceIntelligence(
            wilaya = wilaya,
            commune = commune,
            category = category,
            averagePriceDzd = avg,
            minPriceDzd = min,
            maxPriceDzd = max,
            averagePricePerM2 = avgM2,
            priceTrendPercent = 3.2
        )
    }

    // --- Algerian Wilayas list ---
    val wilayasList: List<Wilaya> = listOf(
        Wilaya("01", "Adrar", "أدرار", listOf("Adrar", "Timimoun", "Reggane")),
        Wilaya("02", "Chlef", "الشلف", listOf("Chlef", "Ténès", "Boukadir")),
        Wilaya("03", "Laghouat", "الأغواط", listOf("Laghouat", "Aflou")),
        Wilaya("04", "Oum El Bouaghi", "أم البواقي", listOf("Oum El Bouaghi", "Aïn Beïda")),
        Wilaya("05", "Batna", "باتنة", listOf("Batna", "Barika", "Aïn Touta")),
        Wilaya("06", "Béjaïa", "بجاية", listOf("Béjaïa", "Amizour", "Akbou", "El Kseur")),
        Wilaya("07", "Biskra", "بسكرة", listOf("Biskra", "Tolga")),
        Wilaya("08", "Béchar", "بشار", listOf("Béchar", "Abadla")),
        Wilaya("09", "Blida", "البليدة", listOf("Ouled Yaich", "Blida Ville", "Boufarik", "El Affroun", "Mouzaia", "Beni Mered")),
        Wilaya("10", "Bouira", "bouira", listOf("Bouira", "Lakhdaria", "Sour El Ghozlane")),
        Wilaya("11", "Tamanrasset", "تمنراست", listOf("Tamanrasset", "In Salah")),
        Wilaya("12", "Tébessa", "تبسة", listOf("Tébessa", "El Aouinet")),
        Wilaya("13", "Tlemcen", "تلمسان", listOf("Tlemcen", "Mansourah", "Maghnia", "Remchi")),
        Wilaya("14", "Tiaret", "تيارت", listOf("Tiaret", "Sougueur")),
        Wilaya("15", "Tizi Ouzou", "تيزي وزو", listOf("Tizi Ouzou", "Draâ Ben Khedda", "Azazga", "Tigzirt")),
        Wilaya("16", "Alger", "الجزائر", listOf("Bab Ezzouar", "Cheraga", "Hydra", "Kouba", "El Biar", "Zeralda", "Sidi M'Hamed", "Dely Ibrahim", "Bir Mourad Raïs")),
        Wilaya("17", "Djelfa", "الجلفة", listOf("Djelfa", "Aïn Oussera")),
        Wilaya("18", "Jijel", "جيجل", listOf("Jijel", "Taher")),
        Wilaya("19", "Sétif", "سطيف", listOf("Sétif", "El Eulma", "Aïn Arnat")),
        Wilaya("20", "Saïda", "سعيدة", listOf("Saïda")),
        Wilaya("21", "Skikda", "سكيكدة", listOf("Skikda", "El Harrouch")),
        Wilaya("22", "Sidi Bel Abbès", "سيدي بلعباس", listOf("Sidi Bel Abbès")),
        Wilaya("23", "Annaba", "عنابة", listOf("Annaba", "El Bouni", "Berrahal")),
        Wilaya("24", "Guelma", "قالمة", listOf("Guelma")),
        Wilaya("25", "Constantine", "قسنطينة", listOf("Constantine", "El Khroub", "Hamma Bouziane", "Nouvelle Ville Ali Mendjeli")),
        Wilaya("26", "Médéa", "المدية", listOf("Médéa", "Berrouaghia")),
        Wilaya("27", "Mostaganem", "مستغانم", listOf("Mostaganem", "Aïn Tedeles")),
        Wilaya("28", "M'Sila", "المسيلة", listOf("M'Sila", "Bou Saâda")),
        Wilaya("29", "Mascara", "معسكر", listOf("Mascara", "Sig")),
        Wilaya("30", "Ouargla", "ورقلة", listOf("Ouargla", "Hassi Messaoud")),
        Wilaya("31", "Oran", "وهران", listOf("Oran Ville", "Bir El Djir", "Es Senia", "Aïn El Turk", "Arzew")),
        Wilaya("35", "Boumerdès", "بومرداس", listOf("Boumerdès", "Bordj Menaïel", "Khemis El Khechna")),
        Wilaya("38", "Tissemsilt", "تيسمسيلت", listOf("Tissemsilt")),
        Wilaya("42", "Tipaza", "تيبازة", listOf("Tipaza", "Cherchell", "Bou Ismail", "Kolea")),
        Wilaya("47", "Ghardaïa", "غرداية", listOf("Ghardaïa", "Metlili"))
    )

    // --- Seed Data Initializer ---
    private suspend fun seedDatabase() {
        val users = listOf(
            User("landlord_ahmed", "Ahmed Benali", "ahmed.b@gmail.com", "+213 551 22 33 44", UserRole.LANDLORD, memberSince = "2024", responseRate = 98, responseTimeText = "10 min"),
            User("landlord_karim", "Karim Ziani", "karim.z@gmail.com", "+213 661 88 99 00", UserRole.LANDLORD, memberSince = "2025", responseRate = 92, responseTimeText = "20 min"),
            User("agency_immo", "Blida Immobilier Agency", "contact@blidaimmo.dz", "+213 25 30 40 50", UserRole.AGENCY, isAgency = true, agencyName = "Sarl Blida Immo", memberSince = "2023", responseRate = 99, responseTimeText = "5 min"),
            User("agency_alger", "Casbah Premier Real Estate", "info@casbahrealestate.dz", "+213 21 66 77 88", UserRole.AGENCY, isAgency = true, agencyName = "Casbah Premier", memberSince = "2022", responseRate = 95, responseTimeText = "15 min"),
            User("tenant_mehdi", "Mehdi K.", "mehdi@kridar.dz", "+213 550 12 34 56", UserRole.TENANT)
        )
        db.userDao().insertUsers(users)

        val properties = listOf(
            Property(
                id = "prop_1",
                title = "Modern F3 Apartment in High Standing Residence",
                description = "Magnificent F3 apartment of 95 m² located in Ouled Yaich, Blida. Built in 2024, includes modern fitted kitchen, central heating, air conditioning, private garage parking, and balcony with mountain view.",
                priceDzd = 85000.0,
                category = PropertyCategory.APARTMENT,
                rentalType = RentalType.MONTHLY,
                wilaya = "Blida",
                commune = "Ouled Yaich",
                neighborhood = "Cité 1000 Logements",
                latitude = 36.4950,
                longitude = 2.8520,
                surfaceM2 = 95,
                bedrooms = 2,
                bathrooms = 2,
                totalRooms = 3,
                floorLevel = 3,
                imageResNames = listOf("img_property_algiers_f3_1786376194767", "img_property_bab_ezzouar_f2_1786376266050"),
                isFurnished = true,
                hasParking = true,
                hasElevator = true,
                hasBalcony = true,
                hasHeating = true,
                hasAc = true,
                hasInternet = true,
                allowsPets = false,
                isFamilyOnly = true,
                isStudentFriendly = false,
                landlordId = "landlord_ahmed",
                landlordName = "Ahmed Benali",
                landlordVerification = VerificationStatus.VERIFIED,
                isVerifiedProperty = true,
                availableFrom = "01 September 2026",
                depositMonths = 2,
                isFeatured = true,
                viewsCount = 2840
            ),
            Property(
                id = "prop_2",
                title = "Luxury Duplex Villa with Private Garden",
                description = "Spacious duplex villa of 280 m² in quiet residential area of Blida Ville. Features 5 bedrooms, double living room, garage for 2 cars, solar water heater, garden, and high-speed fiber internet.",
                priceDzd = 180000.0,
                category = PropertyCategory.VILLA,
                rentalType = RentalType.YEARLY,
                wilaya = "Blida",
                commune = "Blida Ville",
                neighborhood = "Bab El Rahba",
                latitude = 36.4710,
                longitude = 2.8310,
                surfaceM2 = 280,
                bedrooms = 5,
                bathrooms = 3,
                totalRooms = 7,
                floorLevel = 1,
                imageResNames = listOf("img_property_blida_villa_1786376241704", "img_property_algiers_f3_1786376194767"),
                isFurnished = false,
                hasParking = true,
                hasElevator = false,
                hasBalcony = true,
                hasHeating = true,
                hasAc = true,
                hasInternet = true,
                allowsPets = true,
                isFamilyOnly = true,
                landlordId = "agency_immo",
                landlordName = "Blida Immobilier Agency",
                landlordVerification = VerificationStatus.VERIFIED,
                isVerifiedProperty = true,
                availableFrom = "Immediate",
                depositMonths = 3,
                agencyFeeDzd = 40000.0,
                isFeatured = true,
                viewsCount = 3120
            ),
            Property(
                id = "prop_3",
                title = "Cozy Modern Studio near University",
                description = "Stylish studio apartment in Bab Ezzouar, Algiers. Ideal for students or young working professionals. Close to USTHB University, tramway station, and Bab Ezzouar Shopping Mall.",
                priceDzd = 45000.0,
                category = PropertyCategory.STUDIO,
                rentalType = RentalType.STUDENT,
                wilaya = "Alger",
                commune = "Bab Ezzouar",
                neighborhood = "Cité CNEP",
                latitude = 36.7210,
                longitude = 3.1830,
                surfaceM2 = 42,
                bedrooms = 1,
                bathrooms = 1,
                totalRooms = 1,
                floorLevel = 2,
                imageResNames = listOf("img_property_oran_studio_1786376255934", "img_property_bab_ezzouar_f2_1786376266050"),
                isFurnished = true,
                hasParking = false,
                hasElevator = true,
                hasBalcony = false,
                hasHeating = true,
                hasAc = true,
                hasInternet = true,
                allowsPets = false,
                isFamilyOnly = false,
                isStudentFriendly = true,
                landlordId = "landlord_karim",
                landlordName = "Karim Ziani",
                landlordVerification = VerificationStatus.VERIFIED,
                isVerifiedProperty = true,
                availableFrom = "Immediate",
                depositMonths = 1,
                viewsCount = 1950
            ),
            Property(
                id = "prop_4",
                title = "F4 Sea View Apartment in Cheraga",
                description = "Spacious F4 apartment with panoramic sea view in Cheraga, Algiers. 120 m² surface area, 3 bedrooms, 2 bathrooms, modern finishings, 24/7 security guard.",
                priceDzd = 120000.0,
                category = PropertyCategory.APARTMENT,
                rentalType = RentalType.MONTHLY,
                wilaya = "Alger",
                commune = "Cheraga",
                neighborhood = "Résidence Bouchaoui",
                latitude = 36.7610,
                longitude = 2.9520,
                surfaceM2 = 120,
                bedrooms = 3,
                bathrooms = 2,
                totalRooms = 4,
                floorLevel = 4,
                imageResNames = listOf("img_property_bab_ezzouar_f2_1786376266050", "img_property_algiers_f3_1786376194767"),
                isFurnished = true,
                hasParking = true,
                hasElevator = true,
                hasBalcony = true,
                hasHeating = true,
                hasAc = true,
                hasInternet = true,
                allowsPets = false,
                isFamilyOnly = true,
                landlordId = "agency_alger",
                landlordName = "Casbah Premier Real Estate",
                landlordVerification = VerificationStatus.VERIFIED,
                isVerifiedProperty = true,
                availableFrom = "15 September 2026",
                depositMonths = 2,
                isFeatured = true,
                viewsCount = 4200
            ),
            Property(
                id = "prop_5",
                title = "F3 Apartment in Akbou Béjaïa",
                description = "Beautiful clean F3 apartment of 88 m² in Akbou, Béjaïa. High ceiling, bright sunlit rooms, near main commercial street and schools.",
                priceDzd = 55000.0,
                category = PropertyCategory.APARTMENT,
                rentalType = RentalType.FAMILY,
                wilaya = "Béjaïa",
                commune = "Akbou",
                neighborhood = "Centre Ville",
                latitude = 36.4580,
                longitude = 4.5420,
                surfaceM2 = 88,
                bedrooms = 2,
                bathrooms = 1,
                totalRooms = 3,
                floorLevel = 2,
                imageResNames = listOf("img_property_algiers_f3_1786376194767"),
                isFurnished = false,
                hasParking = true,
                hasElevator = false,
                hasBalcony = true,
                hasHeating = true,
                hasAc = false,
                hasInternet = true,
                landlordId = "landlord_ahmed",
                landlordName = "Ahmed Benali",
                landlordVerification = VerificationStatus.VERIFIED,
                isVerifiedProperty = true,
                viewsCount = 980
            ),
            Property(
                id = "prop_6",
                title = "Prime Commercial Space in Oran Ville",
                description = "140 m² ground floor commercial space suitable for shop, pharmacy, showroom or bank branch located on main avenue in Oran Ville.",
                priceDzd = 220000.0,
                category = PropertyCategory.COMMERCIAL,
                rentalType = RentalType.COMMERCIAL,
                wilaya = "Oran",
                commune = "Oran Ville",
                neighborhood = "Boulevard Larbi Ben M'hidi",
                latitude = 35.6980,
                longitude = -0.6340,
                surfaceM2 = 140,
                bedrooms = 0,
                bathrooms = 2,
                totalRooms = 3,
                floorLevel = 0,
                imageResNames = listOf("img_property_oran_studio_1786376255934"),
                isFurnished = false,
                hasParking = true,
                hasElevator = false,
                hasBalcony = false,
                hasHeating = true,
                hasAc = true,
                hasInternet = true,
                landlordId = "agency_immo",
                landlordName = "Blida Immobilier Agency",
                landlordVerification = VerificationStatus.VERIFIED,
                isVerifiedProperty = true,
                viewsCount = 1850
            ),
            Property(
                id = "prop_7_suspicious",
                title = "Unrealistically Cheap Luxury Villa in Hydra",
                description = "Luxury 400 m² villa in Hydra for only 15000 DZD per month! Contact immediately!",
                priceDzd = 15000.0, // Suspicious price flag trigger!
                category = PropertyCategory.VILLA,
                rentalType = RentalType.MONTHLY,
                wilaya = "Alger",
                commune = "Hydra",
                neighborhood = "Val d'Hydra",
                latitude = 36.7510,
                longitude = 3.0420,
                surfaceM2 = 400,
                bedrooms = 6,
                bathrooms = 4,
                totalRooms = 8,
                floorLevel = 1,
                imageResNames = listOf("img_property_blida_villa_1786376241704"),
                landlordId = "unknown_user",
                landlordName = "Suspicious Landlord",
                landlordVerification = VerificationStatus.UNVERIFIED,
                isVerifiedProperty = false,
                isFlaggedSuspicious = true,
                viewsCount = 450
            )
        )
        db.propertyDao().insertProperties(properties)

        // Seed initial conversations & messages
        val conv = Conversation(
            id = "conv_mehdi_ahmed_prop1",
            propertyId = "prop_1",
            propertyTitle = "Modern F3 Apartment in High Standing Residence",
            propertyPriceDzd = 85000.0,
            propertyLocation = "Ouled Yaich, Blida",
            propertyImageRes = "img_property_algiers_f3_1786376194767",
            tenantId = "tenant_mehdi",
            tenantName = "Mehdi K.",
            landlordId = "landlord_ahmed",
            landlordName = "Ahmed Benali",
            lastMessage = "Perfect! See you Saturday at 15:00 for the visit.",
            lastMessageTime = "10:15 AM",
            unreadCount = 1
        )
        db.messageDao().insertConversation(conv)

        val initialMsgs = listOf(
            Message("msg_1", conv.id, "tenant_mehdi", "Mehdi K.", "Salam Alaykoum Ahmed, is the F3 apartment in Ouled Yaich still available?", timestampText = "09:30 AM"),
            Message("msg_2", conv.id, "landlord_ahmed", "Ahmed Benali", "Wa Alaykoum Salam Mehdi! Yes it is available. It was finished in 2024 and fully furnished.", timestampText = "09:42 AM"),
            Message("msg_3", conv.id, "tenant_mehdi", "Mehdi K.", "Great! Can I schedule a visit this Saturday afternoon around 15:00?", timestampText = "10:05 AM"),
            Message("msg_4", conv.id, "landlord_ahmed", "Ahmed Benali", "Perfect! See you Saturday at 15:00 for the visit.", timestampText = "10:15 AM")
        )
        initialMsgs.forEach { db.messageDao().insertMessage(it) }

        // Seed visit request
        val visitReq = VisitRequest(
            id = "visit_1",
            propertyId = "prop_1",
            propertyTitle = "Modern F3 Apartment in High Standing Residence",
            propertyImageRes = "img_property_algiers_f3_1786376194767",
            propertyLocation = "Ouled Yaich, Blida",
            tenantId = "tenant_mehdi",
            tenantName = "Mehdi K.",
            tenantPhone = "+213 550 12 34 56",
            landlordId = "landlord_ahmed",
            visitDate = "Saturday, 15 August 2026",
            visitTime = "15:00",
            numberOfVisitors = 2,
            note = "Family visit for 1 year rental contract.",
            status = VisitStatus.CONFIRMED
        )
        db.visitDao().insertVisit(visitReq)

        // Seed saved search
        db.savedSearchDao().insertSavedSearch(
            SavedSearch(
                id = "search_1",
                userId = "tenant_mehdi",
                name = "F3 in Blida under 90,000 DA",
                wilaya = "Blida",
                commune = "Ouled Yaich",
                category = PropertyCategory.APARTMENT,
                maxPriceDzd = 90000.0,
                minBedrooms = 2,
                alertsEnabled = true
            )
        )
    }
}
