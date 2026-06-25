package com.veritas.reader.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun UpdateAvailableDialog(
    versionName: String,
    changelog: String,
    isDownloading: Boolean,
    downloadProgress: Float,
    downloadError: String?,
    onUpdate: () -> Unit,
    onCancelDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (isDownloading) {
                onCancelDownload()
            }
            onDismiss()
        },
        title = {
            Text(
                text = "New Update Available: $versionName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (isDownloading) {
                    Text(
                        text = "Downloading update: ${(downloadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LinearProgressIndicator(
                        progress = downloadProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                } else {
                    Text(
                        text = "A new version of Veritas Reader is available. Would you like to update?",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    if (downloadError != null) {
                        Text(
                            text = "Download failed: $downloadError",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    if (changelog.isNotBlank()) {
                        Text(
                            text = "Changelog:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = changelog,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isDownloading) {
                Button(onClick = onUpdate) {
                    Text(if (downloadError != null) "Retry Update" else "Update Now")
                }
            }
        },
        dismissButton = {
            if (isDownloading) {
                TextButton(onClick = onCancelDownload) {
                    Text("Cancel")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Later")
                }
            }
        }
    )
}
