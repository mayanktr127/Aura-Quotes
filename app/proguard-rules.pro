# ProGuard and R8 rules for Stillwords

# Keep line numbers and file names for debuggable crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room rules
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-dontwarn androidx.room.paging.**
-keep class com.myplanetservices.stillwords.data.local.** { *; }

# Domain and Data Models
-keep class com.myplanetservices.stillwords.data.model.** { *; }

# Coil rules
-keep class coil.** { *; }
-dontwarn coil.**
-keep class coil.compose.** { *; }

# Kotlinx Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# WorkManager
-keep class * extends androidx.work.Worker {
    <init>(...);
}
-keep class * extends androidx.work.ListenableWorker {
    <init>(...);
}
-keep class * extends androidx.work.CoroutineWorker {
    <init>(...);
}
-keep class com.myplanetservices.stillwords.notification.** { *; }

# Google Play In-App Updates
-keep class com.google.android.play.core.appupdate.** { *; }
-keep class com.google.android.play.core.install.** { *; }
-keep class com.google.android.play.core.common.** { *; }
-dontwarn com.google.android.play.core.**
