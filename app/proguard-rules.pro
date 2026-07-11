# ProGuard rules for Lanzarus v1.1.0
# Firebase AI
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
# Retrofit
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
# Moshi
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
# Keep model/data classes (Moshi needs them)
-keep class com.example.data.model.** { *; }
-keep class com.example.api.** { *; }
# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
# OkHttp / Logging
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class com.squareup.okhttp.** { *; }
# Coil
-keep class coil.** { *; }
# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
# General Android
-keep class * extends androidx.activity.ComponentActivity { *; }
-keep class * implements android.os.Parcelable { *; }
-keep class * implements java.io.Serializable { *; }
