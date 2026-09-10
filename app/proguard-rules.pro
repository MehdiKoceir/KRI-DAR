# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Room Database rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomOpenHelper
-keep class com.example.data.local.** { *; }

# App Models & Entities (preserve fields for SQLite mapping & serialization)
-keep class com.example.data.model.** { *; }
-keepnames class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** {
    <fields>;
    <methods>;
}

# Kotlin Coroutines & Flow
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# Moshi rules
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep class com.squareup.moshi.** { *; }

# Coil image loading
-keep class coil.** { *; }
-dontwarn coil.**

# Preserve line numbers for stack traces
-keepattributes SourceFile,LineNumberTable
