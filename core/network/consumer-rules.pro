# Ktor + kotlinx.serialization - keep generated serializers.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.amro.**$$serializer { *; }
-keepclassmembers class com.amro.** {
    *** Companion;
}
-keepclasseswithmembers class com.amro.** {
    kotlinx.serialization.KSerializer serializer(...);
}
