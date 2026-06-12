# ProGuard rules for Veritas Reader

# 1. Jetpack Compose Rules
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# 2. PDFBox-Android & Bouncy Castle Rules
# These libraries make extensive use of reflection for loading fonts, certificates, and encryption providers.
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**

-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# 3. ML Kit Text Recognition Rules
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.internal.mlkit_**

# 5. Room Persistence Rules
# Required if you are using Room for document storage
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# 6. Keep specific model classes used in serialization/reflection
# These are passed into WorkManager Data or used in UI state serialization
-keep class com.veritas.reader.PdfImportOptions { *; }
-keep class com.veritas.reader.TextImportOptions { *; }
-keep class com.veritas.reader.ui.VeritasPendingImport { *; }
-keep class com.veritas.reader.SavedDocument { *; }
-keep class com.veritas.reader.QueueEntry { *; }
-keep class com.veritas.reader.ReadingHistoryEntry { *; }
-keep class com.veritas.reader.GeneralNote { *; }
-keep class com.veritas.reader.ReaderAnnotation { *; }
-keep class com.veritas.reader.ReaderDocument { *; }
-keep class com.veritas.reader.PronunciationRule { *; }

# 7. Keep Background Workers
-keep class com.veritas.reader.DocumentImportWorker { *; }

# 8. PDFBox-Android Dependency Keep Rules (Harmony AWT backport)
# PDFBox-Android relies heavily on custom package com.tom_roush.harmony for AWT geometries, matrices, and color models.
-keep class com.tom_roush.harmony.** { *; }
-dontwarn com.tom_roush.harmony.**

# 9. Jetpack PDF Viewer Keep Rules
-keep class androidx.pdf.** { *; }
-dontwarn androidx.pdf.**

# 10. Jetpack WorkManager Keep Rules
-keep class androidx.work.impl.WorkManagerInitializer { *; }
-keep class androidx.work.impl.background.systemjob.SystemJobService { *; }
-keep class androidx.work.impl.foreground.SystemForegroundService { *; }
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

