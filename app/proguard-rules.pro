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

# 11. Jetpack Glance Keep Rules
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * implements androidx.glance.appwidget.action.ActionCallback { *; }
-keep class * implements androidx.glance.action.ActionCallback { *; }
-keep class com.veritas.reader.**WidgetReceiver { *; }
-keep class com.veritas.reader.VeritasPlayerWidgetProvider { *; }
-keep class com.veritas.reader.**Callback { *; }
-keep class com.veritas.reader.**Receiver { *; }
-keep class com.veritas.reader.**Provider { *; }

# 12. Sherpa-ONNX, Kokoro, Piper & Native Methods Keep Rules
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.k2fsa.sherpa.onnx.** { *; }
-dontwarn com.k2fsa.sherpa.onnx.**
-keep class com.veritas.reader.tts.** { *; }
-keep class com.veritas.reader.PiperEngine** { *; }
-keep class com.veritas.reader.KokoroEngine** { *; }
-keep class com.veritas.reader.SherpaEngine** { *; }

# 13. Data Models & JSON Serialization Keep Rules
-keep class com.veritas.reader.Flashcard** { *; }
-keep class com.veritas.reader.ReadingGoal** { *; }
-keep class com.veritas.reader.StudyDeck** { *; }
-keep class com.veritas.reader.ReadingInsightsData** { *; }
-keep class com.veritas.reader.WebArticle { *; }
-keep class com.veritas.reader.tts.OfflineVoiceDescriptor { *; }
-keep class com.veritas.reader.tts.NeuralPackageDescriptor { *; }
-keep class com.veritas.reader.tts.DownloadState** { *; }
-keep class com.veritas.reader.NoteReminder** { *; }
-keep class com.veritas.reader.Streak** { *; }
-keep class com.veritas.reader.Math** { *; }
-keep class com.veritas.reader.VeritasReadingList** { *; }
-keep class com.veritas.reader.CrashReporter { *; }
-keep class com.veritas.reader.AutoBackupWorker { *; }
-keep class com.veritas.reader.StreakReminderWorker { *; }


