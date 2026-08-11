package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.PropertyCategory
import com.example.data.model.RentalType
import com.example.data.model.UserRole
import com.example.data.model.VerificationStatus
import com.example.data.model.VisitStatus

class Converters {

    @TypeConverter
    fun fromListToString(list: List<String>?): String {
        return list?.joinToString("|||") ?: ""
    }

    @TypeConverter
    fun toListFromString(data: String?): List<String> {
        if (data.isNullOrEmpty()) return emptyList()
        return data.split("|||").filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = try {
        UserRole.valueOf(value)
    } catch (e: Exception) {
        UserRole.TENANT
    }

    @TypeConverter
    fun fromPropertyCategory(value: PropertyCategory): String = value.name

    @TypeConverter
    fun toPropertyCategory(value: String): PropertyCategory = try {
        PropertyCategory.valueOf(value)
    } catch (e: Exception) {
        PropertyCategory.APARTMENT
    }

    @TypeConverter
    fun fromRentalType(value: RentalType): String = value.name

    @TypeConverter
    fun toRentalType(value: String): RentalType = try {
        RentalType.valueOf(value)
    } catch (e: Exception) {
        RentalType.MONTHLY
    }

    @TypeConverter
    fun fromVerificationStatus(value: VerificationStatus): String = value.name

    @TypeConverter
    fun toVerificationStatus(value: String): VerificationStatus = try {
        VerificationStatus.valueOf(value)
    } catch (e: Exception) {
        VerificationStatus.UNVERIFIED
    }

    @TypeConverter
    fun fromVisitStatus(value: VisitStatus): String = value.name

    @TypeConverter
    fun toVisitStatus(value: String): VisitStatus = try {
        VisitStatus.valueOf(value)
    } catch (e: Exception) {
        VisitStatus.PENDING
    }
}
