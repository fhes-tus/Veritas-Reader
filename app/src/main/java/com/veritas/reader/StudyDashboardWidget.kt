package com.veritas.reader

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.action.ActionParameters
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
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.LinearProgressIndicator

class StudyDashboardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StudyDashboardWidget()
}

class StudyDashboardWidget : GlanceAppWidget() {
    override val sizeMode: androidx.glance.appwidget.SizeMode = androidx.glance.appwidget.SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = DocumentRepository(context)
        val tracker = repository.loadReaderTrackerSnapshot()
        val notesCount = repository.loadGeneralNotes().size

        provideContent {
            val size = androidx.glance.LocalSize.current
            val cal = java.util.Calendar.getInstance()
            val todayKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time)
            val daysByKey = repository.loadTrackerDays()
            val todayMillis = daysByKey[todayKey]?.usageMillis ?: 0L
            val todayMinutes = (todayMillis / 60000L).coerceAtLeast(0L)
            
            val dailyGoalMinutes = 30L
            val goalProgress = (todayMinutes.toFloat() / dailyGoalMinutes.toFloat()).coerceIn(0f, 1f)

            val borderModifier = GlanceModifier
                .fillMaxSize()
                .background(VeritasWidgetColors.border)
                .cornerRadius(24.dp)

            Box(
                modifier = borderModifier,
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(1.dp)
                        .cornerRadius(23.dp)
                        .background(VeritasWidgetColors.frostedBackground)
                        .padding(12.dp)
                        .clickable(actionRunCallback<StudyDashboardClickCallback>())
                ) {
                    // Header
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Study Dashboard",
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
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔥 ${tracker.currentStreak}d",
                                style = TextStyle(
                                    fontSize = if (size.width >= 240.dp) 12.sp else 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(Color(0xFFFAB387))
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    // Stats row
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .cornerRadius(12.dp)
                                .background(VeritasWidgetColors.cardBackground)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Today",
                                    style = TextStyle(
                                        fontSize = if (size.width >= 240.dp) 12.sp else 10.sp,
                                        color = VeritasWidgetColors.textMuted
                                    )
                                )
                                Spacer(modifier = GlanceModifier.height(2.dp))
                                Text(
                                    text = "$todayMinutes m",
                                    style = TextStyle(
                                        fontSize = if (size.width >= 240.dp) 14.sp else 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VeritasWidgetColors.textPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.width(6.dp))

                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .cornerRadius(12.dp)
                                .background(VeritasWidgetColors.cardBackground)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Notes",
                                    style = TextStyle(
                                        fontSize = if (size.width >= 240.dp) 12.sp else 10.sp,
                                        color = VeritasWidgetColors.textMuted
                                    )
                                )
                                Spacer(modifier = GlanceModifier.height(2.dp))
                                Text(
                                    text = "$notesCount",
                                    style = TextStyle(
                                        fontSize = if (size.width >= 240.dp) 14.sp else 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VeritasWidgetColors.primaryAccent
                                    )
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.width(6.dp))

                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .cornerRadius(12.dp)
                                .background(VeritasWidgetColors.cardBackground)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Completed",
                                    style = TextStyle(
                                        fontSize = if (size.width >= 240.dp) 12.sp else 10.sp,
                                        color = VeritasWidgetColors.textMuted
                                    )
                                )
                                Spacer(modifier = GlanceModifier.height(2.dp))
                                Text(
                                    text = "${tracker.documentsCompletedThisMonth}",
                                    style = TextStyle(
                                        fontSize = if (size.width >= 240.dp) 14.sp else 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VeritasWidgetColors.textPrimary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    // Goal bar
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Progress",
                                style = TextStyle(
                                    fontSize = if (size.width >= 240.dp) 12.sp else 10.sp,
                                    color = VeritasWidgetColors.textMuted
                                ),
                                modifier = GlanceModifier.defaultWeight()
                            )
                            Text(
                                text = "$todayMinutes / 30 min",
                                style = TextStyle(
                                    fontSize = if (size.width >= 240.dp) 12.sp else 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VeritasWidgetColors.primaryAccent
                                )
                            )
                        }
                        Spacer(modifier = GlanceModifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = goalProgress,
                            modifier = GlanceModifier.fillMaxWidth().height(4.dp),
                            color = VeritasWidgetColors.primaryAccent,
                            backgroundColor = VeritasWidgetColors.cardBackground
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    // Weekly usage bar chart representation
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        Text(
                            text = "Weekly Chart",
                            style = TextStyle(
                                fontSize = if (size.width >= 240.dp) 12.sp else 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = VeritasWidgetColors.textMuted
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().height(64.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val sundayCal = java.util.Calendar.getInstance().apply {
                                firstDayOfWeek = java.util.Calendar.SUNDAY
                                set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SUNDAY)
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }
                            val sundayToSaturdayKeys = List(7) { index ->
                                val dayCal = (sundayCal.clone() as java.util.Calendar).apply {
                                    add(java.util.Calendar.DAY_OF_YEAR, index)
                                }
                                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(dayCal.time)
                            }
                            val weeklyUsageByDay = sundayToSaturdayKeys.map { key -> daysByKey[key]?.usageMillis ?: 0L }
                            val maxVal = weeklyUsageByDay.maxOrNull()?.coerceAtLeast(1L) ?: 1L
                            val labels = listOf("S", "M", "T", "W", "T", "F", "S")
                            val dayIdx = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1

                            weeklyUsageByDay.forEachIndexed { i, value ->
                                val isToday = i == dayIdx
                                val fraction = (value.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
                                val barHeight = (fraction * 48f).toInt().coerceAtLeast(2)

                                Column(
                                    modifier = GlanceModifier.defaultWeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = GlanceModifier
                                            .width(12.dp)
                                            .height(48.dp)
                                            .cornerRadius(6.dp)
                                            .background(VeritasWidgetColors.cardBackground),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        if (value > 0L) {
                                            Box(
                                                modifier = GlanceModifier
                                                    .width(12.dp)
                                                    .height(barHeight.dp)
                                                    .cornerRadius(6.dp)
                                                    .background(
                                                        if (isToday) VeritasWidgetColors.primaryAccent else VeritasWidgetColors.textMuted
                                                    )
                                            ) {}
                                        }
                                    }
                                    Spacer(modifier = GlanceModifier.height(2.dp))
                                    Text(
                                        text = labels.getOrElse(i) { "" },
                                        style = TextStyle(
                                            fontSize = if (size.width >= 240.dp) 10.sp else 8.sp,
                                            color = if (isToday) VeritasWidgetColors.primaryAccent else VeritasWidgetColors.textMuted
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
}

class StudyDashboardClickCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(MainActivity.EXTRA_WIDGET_ACTION, MainActivity.ACTION_SHOW_STUDY_DASHBOARD)
        }
        context.startActivity(intent)
    }
}
