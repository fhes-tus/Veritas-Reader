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
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.SizeMode

import androidx.glance.ColorFilter

class QuickCaptureWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickCaptureWidget()
}

class QuickCaptureWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val size = androidx.glance.LocalSize.current
            val isCompact = size.width < 180.dp

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(VeritasWidgetColors.border)
                    .cornerRadius(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(1.dp)
                        .cornerRadius(27.dp)
                        .background(VeritasWidgetColors.frostedBackground)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompact) {
                        // 2x1 Layout: Modern Capsule with quick action buttons
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .size(38.dp)
                                    .cornerRadius(12.dp)
                                    .background(VeritasWidgetColors.cardElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.veritas_reader_icon),
                                    contentDescription = "Veritas",
                                    modifier = GlanceModifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            Text(
                                text = "Veritas",
                                style = TextStyle(
                                    fontSize = if (size.width >= 140.dp) 15.sp else 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VeritasWidgetColors.textPrimary
                                ),
                                maxLines = 1,
                                modifier = GlanceModifier.defaultWeight()
                            )
                            
                            val newNoteParams = actionParametersOf(QuickCaptureActionCallback.ActionKey to MainActivity.ACTION_NEW_NOTE)
                            Box(
                                modifier = GlanceModifier
                                    .cornerRadius(16.dp)
                                    .background(VeritasWidgetColors.playButtonAccent)
                                    .clickable(actionRunCallback<QuickCaptureActionCallback>(newNoteParams))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+ Note",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VeritasWidgetColors.playIconColor
                                    )
                                )
                            }
                            
                            Spacer(modifier = GlanceModifier.width(4.dp))
                            
                            Box(
                                modifier = GlanceModifier
                                    .size(34.dp)
                                    .cornerRadius(17.dp)
                                    .background(VeritasWidgetColors.cardBackground)
                                    .clickable(actionRunCallback<LaunchWidgetMenuActivityCallback>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_widget_import),
                                    contentDescription = "More",
                                    modifier = GlanceModifier.size(18.dp),
                                    colorFilter = ColorFilter.tint(VeritasWidgetColors.primaryAccent)
                                )
                            }
                        }
                    } else {
                        // >= 2x2 Grid Visual Layout with Modern Tiles
                        Column(
                            modifier = GlanceModifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Header
                            Row(
                                modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Quick Actions",
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VeritasWidgetColors.textPrimary
                                    ),
                                    modifier = GlanceModifier.defaultWeight()
                                )
                                Box(
                                    modifier = GlanceModifier
                                        .cornerRadius(8.dp)
                                        .background(VeritasWidgetColors.cardBackground)
                                        .clickable(actionRunCallback<LaunchWidgetMenuActivityCallback>())
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Menu ▾",
                                        style = TextStyle(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VeritasWidgetColors.primaryAccent
                                        )
                                    )
                                }
                            }
                            
                            Spacer(modifier = GlanceModifier.height(6.dp))

                            Row(
                                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ActionCard(
                                    iconRes = R.drawable.ic_widget_notes,
                                    label = "Text Note",
                                    action = MainActivity.ACTION_SHOW_NOTES,
                                    modifier = GlanceModifier.defaultWeight()
                                )
                                Spacer(modifier = GlanceModifier.width(6.dp))
                                ActionCard(
                                    iconRes = R.drawable.ic_widget_checklist,
                                    label = "Checklist",
                                    action = MainActivity.ACTION_NEW_CHECKLIST_NOTE,
                                    modifier = GlanceModifier.defaultWeight()
                                )
                            }
                            Spacer(modifier = GlanceModifier.height(6.dp))
                            Row(
                                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ActionCard(
                                    iconRes = R.drawable.ic_widget_reminder,
                                    label = "Reminder",
                                    action = MainActivity.ACTION_NEW_REMINDER_NOTE,
                                    modifier = GlanceModifier.defaultWeight()
                                )
                                Spacer(modifier = GlanceModifier.width(6.dp))
                                ActionCard(
                                    iconRes = R.drawable.ic_widget_reading,
                                    label = "Read Now",
                                    action = MainActivity.ACTION_ACTIVE_READING,
                                    modifier = GlanceModifier.defaultWeight()
                                )
                            }
                            Spacer(modifier = GlanceModifier.height(6.dp))
                            Row(
                                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ActionCard(
                                    iconRes = R.drawable.ic_widget_library,
                                    label = "Library",
                                    action = MainActivity.ACTION_OPEN_LIBRARY,
                                    modifier = GlanceModifier.defaultWeight()
                                )
                                Spacer(modifier = GlanceModifier.width(6.dp))
                                ActionCard(
                                    iconRes = R.drawable.ic_widget_import,
                                    label = "Import Doc",
                                    action = MainActivity.ACTION_IMPORT_DOCUMENTS,
                                    modifier = GlanceModifier.defaultWeight()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ActionCard(
        iconRes: Int,
        label: String,
        action: String,
        modifier: GlanceModifier
    ) {
        val params = actionParametersOf(QuickCaptureActionCallback.ActionKey to action)
        Box(
            modifier = modifier
                .fillMaxHeight()
                .cornerRadius(14.dp)
                .background(VeritasWidgetColors.cardBackground)
                .clickable(actionRunCallback<QuickCaptureActionCallback>(params)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = GlanceModifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(28.dp)
                        .cornerRadius(8.dp)
                        .background(VeritasWidgetColors.cardElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(iconRes),
                        contentDescription = label,
                        modifier = GlanceModifier.size(18.dp),
                        colorFilter = ColorFilter.tint(VeritasWidgetColors.primaryAccent)
                    )
                }
                Spacer(modifier = GlanceModifier.width(6.dp))
                val currentSize = androidx.glance.LocalSize.current
                val fontSize = if (currentSize.width >= 240.dp) 12.sp else 10.sp
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = VeritasWidgetColors.textPrimary
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

class LaunchWidgetMenuActivityCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, WidgetMenuActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }
}

class QuickCaptureActionCallback : ActionCallback {
    companion object {
        val ActionKey = ActionParameters.Key<String>("widget_action")
    }

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val action = parameters[ActionKey] ?: return
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_WIDGET_ACTION, action)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }
}
