-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn okhttp3.**
-dontwarn retrofit2.**

-keep class com.yandex.authsdk.** { *; }
-dontwarn com.yandex.authsdk.**
