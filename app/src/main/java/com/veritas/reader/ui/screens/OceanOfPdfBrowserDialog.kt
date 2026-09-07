package com.veritas.reader.ui.screens

import androidx.compose.runtime.Composable
import java.io.File

/**
 * In-App Browser for OceanOfPDF with automatic download interception.
 * Delegates to [BookCatalogBrowserDialog] with enhanced security, encryption and sandbox privacy.
 */
@Composable
fun OceanOfPdfBrowserDialog(
    initialQuery: String = "",
    onImportDownloadedFile: (File, String) -> Unit,
    onDismiss: () -> Unit
) {
    BookCatalogBrowserDialog(
        initialUrl = "https://oceanofpdf.com/",
        siteName = "Ocean of PDF",
        initialQuery = initialQuery,
        onImportDownloadedFile = onImportDownloadedFile,
        onDismiss = onDismiss
    )
}