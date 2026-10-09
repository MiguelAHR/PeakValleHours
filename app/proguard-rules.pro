# Keep Glance AppWidget receivers and the composables they inflate.
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
-keep class com.peakvalle.hours.widget.** { *; }

# Glance uses reflection on generated RemoteViews classes.
-keep class androidx.glance.** { *; }
-dontwarn androidx.glance.**

# kotlinx.serialization / reflection used by DataStore preferences.
-keepclassmembers class * {
    @androidx.datastore.* <methods>;
}

# Keep line numbers for readable crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile