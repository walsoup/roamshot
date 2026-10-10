# MediaPipe GenAI loads classes reflectively from native code.
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

# ML Kit
-dontwarn com.google.mlkit.**

# Navigation type-safe routes are resolved by serializer.
-keep @kotlinx.serialization.Serializable class app.polaroam.ui.navigation.** { *; }
