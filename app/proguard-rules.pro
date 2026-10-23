# ====================================================================
# ProGuard / R8 Optimization & Keep Rules for FixitBhai
# ====================================================================

# 1. Project Domain Models & Firestore Serialization
-keep class com.example.fixitbhai.Contact { *; }
-keepclassmembers class com.example.fixitbhai.** {
    @com.google.firebase.firestore.PropertyName *;
    @com.google.firebase.firestore.Exclude *;
    <fields>;
    <methods>;
}

# 2. ZXing QR Code Scanner & Barcode Encoder Rules
-keep class com.journeyapps.barcodescanner.** { *; }
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# 3. Firebase Authentication & Firestore Suppressions
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.firestore.**
-dontwarn com.google.gson.**

# 4. AndroidX Navigation & Material 3 UI Components
-keep class androidx.navigation.** { *; }
-keep class com.google.android.material.** { *; }

# 5. Production Performance: Strip Debug Logs in Release Builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}