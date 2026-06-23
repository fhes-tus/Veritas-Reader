package com.veritas.reader

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
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
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.SizeMode
import androidx.compose.runtime.Composable
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.ColorFilter

class PinnedNoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PinnedNoteWidget()
}

class PinnedNoteWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = DocumentRepository(context)
        val pinnedNote = repository.loadGeneralNotes().firstOrNull { it.pinned }

        provideContent {
            val size = androidx.glance.LocalSize.current
            val isExpanded = size.height >= 140.dp

            val borderModifier = GlanceModifier
                .fillMaxSize()
                .background(VeritasWidgetColors.border)
                .cornerRadius(24.dp)

            if (pinnedNote == null) {
                Box(
                    modifier = borderModifier,
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .padding(1.dp)
                            .cornerRadius(23.dp)
                            .background(VeritasWidgetColors.frostedBackground)
                            .clickable(actionRunCallback<LaunchAppActionCallback>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No pinned notes yet",
                            style = TextStyle(
                                fontSize = if (size.width >= 240.dp) 15.sp else 13.sp,
                                color = VeritasWidgetColors.textMuted
                            )
                        )
                    }
                }
            } else {
                val clickParams = actionParametersOf(NoteClickCallback.NoteIdKey to pinnedNote.id)
                val newNoteParams = actionParametersOf(QuickCaptureActionCallback.ActionKey to MainActivity.ACTION_NEW_NOTE)

                Box(
                    modifier = borderModifier,
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .padding(1.dp)
                            .cornerRadius(23.dp)
                            .background(VeritasWidgetColors.frostedBackground)
                            .padding(12.dp)
                            .clickable(actionRunCallback<NoteClickCallback>(clickParams)),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Column(
                            modifier = GlanceModifier.fillMaxSize(),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Title Bar
                            Row(
                                modifier = GlanceModifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pinnedNote.title.ifBlank { "Untitled Note" },
                                    style = TextStyle(
                                        fontSize = if (size.width >= 240.dp) 16.sp else 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VeritasWidgetColors.textPrimary
                                    ),
                                    maxLines = 1,
                                    modifier = GlanceModifier.defaultWeight()
                                )
                                Spacer(modifier = GlanceModifier.width(6.dp))
                                Text(
                                    text = "📌",
                                    style = TextStyle(fontSize = if (size.width >= 240.dp) 14.sp else 12.sp)
                                )
                            }

                            Spacer(modifier = GlanceModifier.height(6.dp))

                            // Content / Checklist preview
                            Column(
                                modifier = GlanceModifier.defaultWeight().fillMaxWidth()
                            ) {
                                if (pinnedNote.isChecklist) {
                                    val lines = pinnedNote.content.split("\n").filter { it.isNotBlank() }
                                    val limit = if (isExpanded) 5 else 3
                                    lines.take(limit).forEachIndexed { index, line ->
                                        val isChecked = line.startsWith("[x]")
                                        val text = line.removePrefix("[ ] ").removePrefix("[x] ").removePrefix("[ ]").removePrefix("[x]")
                                        val toggleParams = actionParametersOf(
                                            ToggleChecklistItemCallback.NoteIdKey to pinnedNote.id,
                                            ToggleChecklistItemCallback.ItemIndexKey to index
                                        )
                                        Row(
                                            modifier = GlanceModifier
                                                .fillMaxWidth()
                                                .padding(vertical = 1.dp)
                                                .clickable(actionRunCallback<ToggleChecklistItemCallback>(toggleParams)),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Image(
                                                provider = ImageProvider(
                                                    if (isChecked) R.drawable.ic_widget_checklist else R.drawable.ic_widget_checkbox_outline
                                                ),
                                                contentDescription = if (isChecked) "Checked" else "Unchecked",
                                                modifier = GlanceModifier.size(16.dp),
                                                colorFilter = ColorFilter.tint(
                                                    if (isChecked) VeritasWidgetColors.primaryAccent else VeritasWidgetColors.textMuted
                                                )
                                            )
                                            Spacer(modifier = GlanceModifier.width(8.dp))
                                            Text(
                                                text = text,
                                                style = TextStyle(
                                                    fontSize = if (size.width >= 240.dp) 14.sp else 12.sp,
                                                    color = if (isChecked) VeritasWidgetColors.textMuted else VeritasWidgetColors.textPrimary
                                                ),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    if (lines.size > limit) {
                                        Spacer(modifier = GlanceModifier.height(2.dp))
                                        Text(
                                            text = "+ ${lines.size - limit} more items",
                                            style = TextStyle(fontSize = if (size.width >= 240.dp) 12.sp else 10.sp, color = VeritasWidgetColors.textMuted)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = pinnedNote.content.ifBlank { "(Empty)" },
                                        style = TextStyle(
                                            fontSize = if (size.width >= 240.dp) 14.sp else 12.sp,
                                            color = VeritasWidgetColors.textMuted
                                        ),
                                        maxLines = if (isExpanded) 6 else 3
                                    )
                                }
                            }

                            Spacer(modifier = GlanceModifier.height(6.dp))

                            // Bottom Bar
                            if (isExpanded) {
                                Row(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = GlanceModifier.defaultWeight(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        QuickActionIcon(
                                            iconRes = R.drawable.ic_widget_checklist,
                                            action = MainActivity.ACTION_NEW_CHECKLIST_NOTE
                                        )
                                        Spacer(modifier = GlanceModifier.width(8.dp))
                                        QuickActionIcon(
                                            iconRes = R.drawable.ic_widget_voice,
                                            action = MainActivity.ACTION_VOICE_NOTE
                                        )
                                        Spacer(modifier = GlanceModifier.width(8.dp))
                                        QuickActionIcon(
                                            iconRes = R.drawable.ic_widget_reminder,
                                            action = MainActivity.ACTION_NEW_REMINDER_NOTE
                                        )
                                        Spacer(modifier = GlanceModifier.width(8.dp))
                                        QuickActionIcon(
                                            iconRes = R.drawable.ic_widget_camera,
                                            action = MainActivity.ACTION_NEW_IMAGE_NOTE
                                        )
                                    }

                                    Box(
                                        modifier = GlanceModifier
                                            .size(32.dp)
                                            .cornerRadius(16.dp)
                                            .background(VeritasWidgetColors.playButtonAccent)
                                            .clickable(actionRunCallback<QuickCaptureActionCallback>(newNoteParams)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+",
                                            style = TextStyle(
                                                fontSize = if (size.width >= 240.dp) 20.sp else 18.sp, 
                                                fontWeight = FontWeight.Bold, 
                                                color = VeritasWidgetColors.playIconColor
                                            )
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(modifier = GlanceModifier.defaultWeight())
                                    Box(
                                        modifier = GlanceModifier
                                            .size(32.dp)
                                            .cornerRadius(16.dp)
                                            .background(VeritasWidgetColors.playButtonAccent)
                                            .clickable(actionRunCallback<QuickCaptureActionCallback>(newNoteParams)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+",
                                            style = TextStyle(
                                                fontSize = if (size.width >= 240.dp) 20.sp else 18.sp, 
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
    }

    @Composable
    private fun QuickActionIcon(
        iconRes: Int,
        action: String
    ) {
        val params = actionParametersOf(QuickCaptureActionCallback.ActionKey to action)
        Box(
            modifier = GlanceModifier
                .size(32.dp)
                .cornerRadius(16.dp)
                .background(VeritasWidgetColors.cardBackground)
                .clickable(actionRunCallback<QuickCaptureActionCallback>(params)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = "Action",
                modifier = GlanceModifier.size(16.dp),
                colorFilter = ColorFilter.tint(VeritasWidgetColors.textPrimary)
            )
        }
    }
}

class ToggleChecklistItemCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val noteId = parameters[NoteIdKey] ?: return
        val itemIndex = parameters[ItemIndexKey] ?: return

        val repository = DocumentRepository(context)
        val notes = repository.loadGeneralNotes().toMutableList()
        val index = notes.indexOfFirst { it.id == noteId }
        if (index != -1) {
            val note = notes[index]
            val lines = note.content.split("\n").filter { it.isNotBlank() }.toMutableList()
            if (itemIndex in lines.indices) {
                val line = lines[itemIndex]
                val toggledLine = when {
                    line.startsWith("[x] ") -> "[ ] " + line.removePrefix("[x] ")
                    line.startsWith("[ ] ") -> "[x] " + line.removePrefix("[ ] ")
                    line.startsWith("[x]") -> "[ ]" + line.removePrefix("[x]")
                    line.startsWith("[ ]") -> "[x]" + line.removePrefix("[ ]")
                    else -> line
                }
                lines[itemIndex] = toggledLine
                notes[index] = note.copy(content = lines.joinToString("\n"), updatedAt = System.currentTimeMillis())
                repository.saveGeneralNotes(notes)
            }
        }

        updateVeritasWidgets(context)
    }

    companion object {
        val NoteIdKey = ActionParameters.Key<String>("note_id")
        val ItemIndexKey = ActionParameters.Key<Int>("item_index")
    }
}
