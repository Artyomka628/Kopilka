# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep model classes used by Moshi for serialization
-keep class com.squareup.moshi.** { *; }
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keep class *JsonAdapter { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class com.example.model.** { *; }

# Keep ViewModel class fields and methods
-keep class com.example.ui.KopilkaViewModel { *; }

# Prevent R8 from optimizing away Kotlin metadata or Json annotations
-keepclassmembers class * {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}
