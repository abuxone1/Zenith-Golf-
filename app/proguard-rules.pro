# ProGuard / R8 Keep Rules for Zenith Golf 2.0 Release App Bundle

# Preserve Kotlin metadata & Moshi codegen adapters
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
    @com.squareup.moshi.JsonClass *;
}

# Preserve Firebase Cloud Firestore POJOs (Zenith Golf 2.0 Cloud Models)
-keep class com.example.game.firebase.** { *; }
-keepclassmembers class com.example.game.firebase.** { *; }

# Preserve Retrofit / OkHttp / Coroutines stack frames
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
