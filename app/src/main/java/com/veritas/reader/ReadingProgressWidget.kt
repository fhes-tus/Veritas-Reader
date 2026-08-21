package com.veritas.reader

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.Image
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.glance.appwidget.LinearProgressIndicator
import java.io.File
import androidx.glance.appwidget.cornerRadius
import androidx.glance.ColorFilter
import androidx.glance.appwidget.SizeMode

class ReadingProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ReadingProgressWidget()
}

class ReadingProgressWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = DocumentRepository(context)
        val documents = repository.loadDocuments()
        val tracker = repository.loadReaderTrackerSnapshot()
        
        // Find the active document
        val activeId = PlaybackStateStore.activeDocumentId
        val activeDoc = if (activeId != null) {
            repository.findDocument(activeId)
        } else {
            // Fallback to the continue document or most recent
            documents.filter { it.chunkCount > 1 && it.currentIndex in 1 until it.chunkCount }
                .maxByOrNull { it.updatedAt } ?: documents.firstOrNull()
        }

        provideContent {
            val size = androidx.glance.LocalSize.current
            val borderModifier = GlanceModifier
                .fillMaxSize()
                .background(VeritasWidgetColors.border)
                .cornerRadius(28.dp)

            Box(
                modifier = borderModifier,
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(1.dp)
                        .cornerRadius(27.dp)
                        .background(VeritasWidgetColors.frostedBackground)
                        .padding(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reading Tracker",
                            style = TextStyle(
                                fontSize = if (size.width >= 240.dp) 15.sp else 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = VeritasWidgetColors.textPrimary
                            ),
                            modifier = GlanceModifier.defaultWeight()
                        )
                        
                        Box(
                            modifier = GlanceModifier
                                .cornerRadius(12.dp)
                                .background(VeritasWidgetColors.cardBackground)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔥 ${tracker.currentStreak}d Streak",
                                style = TextStyle(
                                    fontSize = if (size.width >= 240.dp) 11.sp else 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VeritasWidgetColors.streakAccent
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    if (activeDoc == null) {
                        Box(
                            modifier = GlanceModifier.defaultWeight().fillMaxWidth().clickable(actionRunCallback<LaunchAppActionCallback>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No active book. Tap to open library.",
                                style = TextStyle(
                                    fontSize = if (size.width >= 240.dp) 14.sp else 12.sp,
                                    color = VeritasWidgetColors.textMuted
                                )
                            )
                        }
                    } else {
                        val progressVal = if (activeDoc.chunkCount > 0) {
                            val currIndex = if (PlaybackStateStore.activeDocumentId == activeDoc.id) {
                                PlaybackStateStore.currentIndex
                            } else {
                                activeDoc.currentIndex
                            }
                            (currIndex.toFloat() / activeDoc.chunkCount.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        val progressPercent = (progressVal * 100).toInt()

                        val todayMinutes = run {
                            val cal = java.util.Calendar.getInstance()
                            val dayIdx = (cal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
                            val millis = tracker.weeklyUsageByDay.getOrNull(dayIdx) ?: 0L
                            (millis / 60000L).coerceAtLeast(0L)
                        }

                        val continueParams = actionParametersOf(
                            ContinueReadingActionCallback.DocIdKey to activeDoc.id
                        )

                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .defaultWeight()
                                .cornerRadius(16.dp)
                                .background(VeritasWidgetColors.cardBackground)
                                .padding(10.dp)
                                .clickable(actionRunCallback<ContinueReadingActionCallback>(continueParams)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = GlanceModifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val coverFile = CoverExtractor.coverFile(context, activeDoc.id)
                                val bitmap = if (coverFile != null && coverFile.exists()) {
                                    runCatching { BitmapFactory.decodeFile(coverFile.absolutePath) }.getOrNull()
                                } else null

                                if (bitmap != null) {
                                    Image(
                                        provider = ImageProvider(bitmap),
                                        contentDescription = "Cover",
                                        modifier = GlanceModifier.size(42.dp, 58.dp).cornerRadius(8.dp)
                                    )
                                } else {
                                    // Premium gradient card cover placeholder
                                    Box(
                                        modifier = GlanceModifier
                                            .size(42.dp, 58.dp)
                                            .cornerRadius(8.dp)
                                            .background(ImageProvider(R.drawable.ic_widget_book_cover_gradient)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            provider = ImageProvider(R.drawable.ic_widget_library),
                                            contentDescription = "Book icon",
                                            modifier = GlanceModifier.size(24.dp),
                                            colorFilter = ColorFilter.tint(ColorProvider(Color.White))
                                        )
                                    }
                                }

                                Spacer(modifier = GlanceModifier.width(10.dp))

                                Column(
                                    modifier = GlanceModifier.defaultWeight()
                                ) {
                                    Text(
                                        text = activeDoc.title,
                                        style = TextStyle(
                                            fontSize = if (size.width >= 240.dp) 14.sp else 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VeritasWidgetColors.textPrimary
                                        ),
                                        maxLines = 1
                                    )
                                    Spacer(modifier = GlanceModifier.height(2.dp))
                                    Text(
                                        text = "$progressPercent% complete • $todayMinutes min today",
                                        style = TextStyle(
                                            fontSize = if (size.width >= 240.dp) 12.sp else 10.sp,
                                            color = VeritasWidgetColors.textMuted
                                        ),
                                        maxLines = 1
                                    )
                                    Spacer(modifier = GlanceModifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = progressVal,
                                        modifier = GlanceModifier.fillMaxWidth().height(4.dp),
                                        color = VeritasWidgetColors.primaryAccent,
                                        backgroundColor = VeritasWidgetColors.progressTrack
                                    )
                                }
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(6.dp))

                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Goal: 30 min",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = VeritasWidgetColors.textMuted
                                )
                            )
                            Spacer(modifier = GlanceModifier.defaultWeight())
                            Box(
                                modifier = GlanceModifier
                                    .cornerRadius(14.dp)
                                    .background(VeritasWidgetColors.playButtonAccent)
                                    .clickable(actionRunCallback<ContinueReadingActionCallback>(continueParams))
                                    .padding(horizontal = 14.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Resume ▶",
                                    style = TextStyle(
                                        fontSize = if (size.width >= 240.dp) 13.sp else 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VeritasWidgetColors.playIconColor
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class LaunchAppActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(intent)
    }
}

class ContinueReadingActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val docId = parameters[DocIdKey] ?: return
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(MainActivity.EXTRA_WIDGET_ACTION, MainActivity.ACTION_CONTINUE_READING)
            putExtra(MainActivity.EXTRA_DOCUMENT_ID, docId)
        }
        context.startActivity(intent)
    }

    companion object {
        val DocIdKey = ActionParameters.Key<String>("doc_id")
    }
}
