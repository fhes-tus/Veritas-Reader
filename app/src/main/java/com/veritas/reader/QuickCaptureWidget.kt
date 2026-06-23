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
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompact) {
                        // 2x1 Layout: Rounded Capsule
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            Text(
                                text = "Veritas",
                                style = TextStyle(
                                    fontSize = if (size.width >= 130.dp) 15.sp else 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VeritasWidgetColors.textPrimary
                                ),
                                maxLines = 1,
                                modifier = GlanceModifier.defaultWeight()
                            )
                            Box(
                                modifier = GlanceModifier
                                    .size(44.dp)
                                    .clickable(actionRunCallback<LaunchWidgetMenuActivityCallback>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = GlanceModifier
                                        .size(32.dp)
                                        .cornerRadius(16.dp)
                                        .background(VeritasWidgetColors.playButtonAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+",
                                        style = TextStyle(
                                            fontSize = if (size.width >= 130.dp) 20.sp else 18.sp, 
                                            fontWeight = FontWeight.Bold, 
                                            color = VeritasWidgetColors.playIconColor
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        // >= 2x2 Grid Visual Layout: 3 rows of 2 columns each (6 options)
                        Column(
                            modifier = GlanceModifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ActionCard(
                                    iconRes = R.drawable.ic_widget_notes,
                                    label = "Text Notes",
                                    action = MainActivity.ACTION_SHOW_NOTES,
                                    modifier = GlanceModifier.defaultWeight()
                                )
                                Spacer(modifier = GlanceModifier.width(6.dp))
                                ActionCard(
                                    iconRes = R.drawable.ic_widget_checklist,
                                    label = "List",
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
                                    label = "Active Reading",
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
                                    label = "Import Documents",
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
                .cornerRadius(12.dp)
                .background(VeritasWidgetColors.cardBackground)
                .clickable(actionRunCallback<QuickCaptureActionCallback>(params)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = GlanceModifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(iconRes),
                    contentDescription = label,
                    modifier = GlanceModifier.size(20.dp),
                    colorFilter = ColorFilter.tint(VeritasWidgetColors.primaryAccent)
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                val currentSize = androidx.glance.LocalSize.current
                val isLongLabel = label.length > 10
                val fontSize = if (currentSize.width >= 240.dp) {
                    if (isLongLabel) 11.sp else 13.sp
                } else {
                    if (isLongLabel) 9.sp else 11.sp
                }
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
