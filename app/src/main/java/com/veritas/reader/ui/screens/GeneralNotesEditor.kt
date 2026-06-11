package com.veritas.reader.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veritas.reader.GeneralNote

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralNotesEditor(
    note: GeneralNote?,
    onSave: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var contentValue by remember { mutableStateOf(TextFieldValue(note?.content ?: "")) }
    val scrollState = rememberScrollState()

    // Helper to insert markdown styling around selection or cursor
    fun insertMarkdown(syntax: String) {
        val text = contentValue.text
        val selection = contentValue.selection
        val start = selection.min
        val end = selection.max
        
        val newText = if (start != end) {
            // Text is selected: wrap it
            text.substring(0, start) + syntax + text.substring(start, end) + syntax + text.substring(end)
        } else {
            // No selection: just insert syntax
            text.substring(0, start) + syntax + syntax + text.substring(start)
        }
        
        // Move selection inside the syntax tags if no selection originally, or after
        val newSelectionStart = if (start != end) end + syntax.length * 2 else start + syntax.length
        contentValue = TextFieldValue(
            text = newText,
            selection = androidx.compose.ui.text.TextRange(newSelectionStart)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (note == null) "New Note" else "Edit Note", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        // Auto-save on back if title or content is not blank
                        if (title.isNotBlank() || contentValue.text.isNotBlank()) {
                            onSave(title, contentValue.text)
                        } else {
                            onDismiss()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (note != null) {
                        IconButton(onClick = { onDelete(note.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Note", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(onClick = {
                        onSave(title, contentValue.text)
                    }) {
                        Icon(Icons.Filled.Done, contentDescription = "Save Note", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            // Note editor helper toolbar (Bold, Italic)
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { insertMarkdown("**") },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.defaultMinSize(minWidth = 44.dp, minHeight = 36.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("B", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    FilledTonalButton(
                        onClick = { insertMarkdown("*") },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.defaultMinSize(minWidth = 44.dp, minHeight = 36.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("I", fontStyle = FontStyle.Italic, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${contentValue.text.length} characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Title input
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        ),
                        placeholder = {
                            Text(
                                "Title",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    // Horizontal line separator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )

                    // Content body input
                    TextField(
                        value = contentValue,
                        onValueChange = { contentValue = it },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        placeholder = {
                            Text(
                                "Start typing your note here...\n\nUse B/I formatting toolbar below.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}
