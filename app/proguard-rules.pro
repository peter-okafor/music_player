# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Media3 classes
-keep class androidx.media3.** { *; }
-keep interface androidx.media3.** { *; }

# Keep Hilt classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep data classes
-keep class com.musicplayer.data.model.** { *; }
