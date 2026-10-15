# ProGuard / R8 Keep Rules for FixitBhai

# Keep Firestore and Gson model classes and properties
-keepclassmembers class com.example.fixitbhai.Contact { <fields>; <methods>; }
-keep class com.example.fixitbhai.Contact { *; }
