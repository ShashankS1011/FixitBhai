# ProGuard / R8 Keep Rules for FixitBhai

# Keep all project models and Firestore serialization properties
-keep class com.example.fixitbhai.** { *; }
-keepclassmembers class com.example.fixitbhai.** {
    @com.google.firebase.firestore.PropertyName *;
    <fields>;
    <methods>;
}

# Keep Firebase and Gson classes
-dontwarn com.google.firebase.firestore.**
-dontwarn com.google.gson.**
