# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# --- Stack traces ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- IME Service (referenced in AndroidManifest.xml) ---
-keep class com.miyabi_hiroshi.app.justarray.ime.JustArrayIME { *; }

# --- Room ---
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# --- DataStore ---
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# --- Timber: strip debug logs in release ---
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
}
