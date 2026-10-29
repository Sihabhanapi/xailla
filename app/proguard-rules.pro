# ============ XRAY CORE ============
-keep class libv2ray.** { *; }
-keepclassmembers class libv2ray.** { *; }
-keep class go.** { *; }
-keepclassmembers class go.** { *; }

# Native methods (JNI)
-keepclasseswithmembernames class * {
    native <methods>;
}

# ============ APP ============
-keep class com.xaillaclass.premium.v2ray.** { *; }
-keep class com.xaillaclass.premium.model.** { *; }
-keep class com.xaillaclass.premium.util.** { *; }

# ============ GSON ============
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ============ KOTLIN ============
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# ============ WARNINGS ============
-dontwarn libv2ray.**
-dontwarn go.**
-dontwarn org.slf4j.**
-dontwarn okhttp3.**
-dontwarn okio.**
