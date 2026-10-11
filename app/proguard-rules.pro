# LiteRT-LM and ONNX Runtime bind Kotlin/Java classes from native code over JNI.
-keep class com.google.ai.edge.litertlm.** { *; }
-dontwarn com.google.ai.edge.litertlm.**
-keep class ai.onnxruntime.** { *; }
-dontwarn ai.onnxruntime.**

# Navigation type-safe routes are resolved by serializer.
-keep @kotlinx.serialization.Serializable class app.polaroam.ui.navigation.** { *; }
