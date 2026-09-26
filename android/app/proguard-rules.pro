# Capacitor & R8 Optimization Keep Rules
-keep class com.getcapacitor.** { *; }
-keep class com.main.arrowrunner.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
