package com.veritas.reader

import android.content.Context
import android.content.Intent
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
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.SizeMode
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

import androidx.glance.appwidget.cornerRadius

import androidx.glance.ColorFilter

@Suppress("RestrictedApi") // Glance's resource-backed ColorProvider is its widget color API.
object VeritasWidgetColors {
    val frostedBackground = ColorProvider(R.color.widget_frosted_background)
    val border = ColorProvider(R.color.widget_border)
    val textPrimary = ColorProvider(R.color.widget_text_primary)
    val textMuted = ColorProvider(R.color.widget_text_muted)
    val cardBackground = ColorProvider(R.color.widget_card_background)
    val widgetBackground = ColorProvider(R.color.widget_background)
    val playButtonAccent = ColorProvider(R.color.widget_play_button_accent)
    val playIconColor = ColorProvider(R.color.widget_play_icon_color)
    val primaryAccent = ColorProvider(R.color.widget_primary_accent)
}

class VeritasPlayerWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VeritasPlayerWidget()
}

class VeritasPlayerWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val size = androidx.glance.LocalSize.current
            val title = PlaybackStateStore.documentTitle.ifBlank { "Veritas Reader" }
            val isPlaying = PlaybackStateStore.isPlaying
            val progress = if (PlaybackStateStore.chunkCount > 0) {
                "Sentence ${PlaybackStateStore.currentIndex + 1} of ${PlaybackStateStore.chunkCount}"
            } else {
                PlaybackStateStore.statusMessage
            }

            val playParams = actionParametersOf(PlayerActionCallback.ActionKey to (if (isPlaying) PlaybackActions.ACTION_PAUSE else PlaybackActions.ACTION_PLAY))
            val prevParams = actionParametersOf(PlayerActionCallback.ActionKey to PlaybackActions.ACTION_PREVIOUS)
            val nextParams = actionParametersOf(PlayerActionCallback.ActionKey to PlaybackActions.ACTION_NEXT)

            val isCompact = size.width < 160.dp

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(VeritasWidgetColors.border)
                    .cornerRadius(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(1.dp)
                        .cornerRadius(23.dp)
                        .background(VeritasWidgetColors.frostedBackground)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable(actionRunCallback<PlayerWidgetClickCallback>()),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Veritas Icon inside a clean white card container
                        Box(
                            modifier = GlanceModifier
                                .size(36.dp)
                                .cornerRadius(8.dp)
                                .background(ColorProvider(Color.White)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.veritas_reader_icon),
                                contentDescription = "Veritas",
                                modifier = GlanceModifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(10.dp))

                        if (isCompact) {
                            // 2x1 layout
                            Text(
                                text = title,
                                style = TextStyle(
                                    fontSize = if (size.width >= 140.dp) 14.sp else 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VeritasWidgetColors.textPrimary
                                ),
                                maxLines = 1,
                                modifier = GlanceModifier.defaultWeight()
                            )

                            Spacer(modifier = GlanceModifier.width(6.dp))

                            Box(
                                modifier = GlanceModifier
                                    .size(36.dp)
                                    .cornerRadius(18.dp)
                                    .background(VeritasWidgetColors.playButtonAccent)
                                    .clickable(actionRunCallback<PlayerActionCallback>(playParams)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(
                                        if (isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
                                    ),
                                    contentDescription = "Play/Pause",
                                    modifier = GlanceModifier.size(20.dp),
                                    colorFilter = ColorFilter.tint(VeritasWidgetColors.playIconColor)
                                )
                            }
                        } else {
                            // Larger layout
                            Column(
                                modifier = GlanceModifier.defaultWeight()
                            ) {
                                Text(
                                    text = title,
                                    style = TextStyle(
                                        fontSize = if (size.width >= 240.dp) 16.sp else 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VeritasWidgetColors.textPrimary
                                    ),
                                    maxLines = 1
                                )
                                Spacer(modifier = GlanceModifier.height(2.dp))
                                Text(
                                    text = progress,
                                    style = TextStyle(
                                        fontSize = if (size.width >= 240.dp) 13.sp else 11.sp,
                                        color = VeritasWidgetColors.textMuted
                                    ),
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = GlanceModifier.width(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_m3_chevron_left),
                                    contentDescription = "Previous",
                                    modifier = GlanceModifier
                                        .size(28.dp)
                                        .clickable(actionRunCallback<PlayerActionCallback>(prevParams)),
                                    colorFilter = ColorFilter.tint(VeritasWidgetColors.textPrimary)
                                )
                                
                                Spacer(modifier = GlanceModifier.width(4.dp))
                                
                                Box(
                                    modifier = GlanceModifier
                                        .size(38.dp)
                                        .cornerRadius(19.dp)
                                        .background(VeritasWidgetColors.playButtonAccent)
                                        .clickable(actionRunCallback<PlayerActionCallback>(playParams)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        provider = ImageProvider(
                                            if (isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
                                        ),
                                        contentDescription = "Play/Pause",
                                        modifier = GlanceModifier.size(22.dp),
                                        colorFilter = ColorFilter.tint(VeritasWidgetColors.playIconColor)
                                    )
                                }
                                
                                Spacer(modifier = GlanceModifier.width(4.dp))
                                
                                Image(
                                    provider = ImageProvider(R.drawable.ic_m3_chevron_right),
                                    contentDescription = "Next",
                                    modifier = GlanceModifier
                                        .size(28.dp)
                                        .clickable(actionRunCallback<PlayerActionCallback>(nextParams)),
                                    colorFilter = ColorFilter.tint(VeritasWidgetColors.textPrimary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class PlayerWidgetClickCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val activeDocId = PlaybackStateStore.activeDocumentId
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (!activeDocId.isNullOrBlank()) {
                putExtra(MainActivity.EXTRA_WIDGET_ACTION, MainActivity.ACTION_CONTINUE_READING)
                putExtra(MainActivity.EXTRA_DOCUMENT_ID, activeDocId)
            }
        }
        context.startActivity(intent)
    }
}

class PlayerActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val action = parameters[ActionKey] ?: return
        val intent = Intent(context, PlaybackService::class.java).setAction(action)
        context.startService(intent)
        // Force widgets to refresh soon
        updateVeritasWidgets(context)
    }

    companion object {
        val ActionKey = ActionParameters.Key<String>("playback_action")
    }
}

fun updateVeritasWidgets(context: Context) {
    val appContext = context.applicationContext
    GlobalScope.launch(Dispatchers.Main) {
        runCatching { VeritasPlayerWidget().updateAll(appContext) }
        runCatching { QuickNoteWidget().updateAll(appContext) }
        runCatching { PinnedNoteWidget().updateAll(appContext) }
        runCatching { QuickCaptureWidget().updateAll(appContext) }
        runCatching { ReadingProgressWidget().updateAll(appContext) }
        runCatching { StudyDashboardWidget().updateAll(appContext) }
    }
}
