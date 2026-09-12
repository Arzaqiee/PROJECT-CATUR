# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep chess engine/model classes (used via reflection-free but kept for safety in release builds)
-keep class com.qie.chess.model.** { *; }
-keep class com.qie.chess.engine.** { *; }
