package com.veritas.reader.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * In-App Browser for OceanOfPDF with automatic download interception.
 * Intercepts EPUB and PDF downloads directly and imports them seamlessly into Veritas Reader.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OceanOfPdfBrowserDialog(
    initialQuery: String = "",
    onImportDownloadedFile: (File, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val initialUrl = remember(initialQuery) {
        if (initialQuery.isNotBlank()) {
            val encoded = Uri.encode(initialQuery.trim())
            "https://oceanofpdf.com/?s=$encoded"
        } else {
            "https://oceanofpdf.com/"
        }
    }

    var currentUrl by remember { mutableStateOf(initialUrl) }
    var lastKnownBookTitle by remember { mutableStateOf("") }
    var pageTitle by remember { mutableStateOf("Ocean of PDF") }
    var pageProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadStatus by remember { mutableStateOf("") }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    fun sanitizeBookTitle(raw: String): String {
        return raw
            .replace(Regex("""(?i)[/_(\[]?OceanofPDF(\.com)?[/_\])]?"""), "")
            .replace(Regex("""(?i)\bOceanofPDF(\.com)?\b"""), "")
            .replace(Regex("""(?i)\[(PDF|EPUB)\]"""), "")
            .replace(Regex("""(?i)\((PDF|EPUB)\)"""), "")
            .replace(Regex("""(?i)\bDownload\b"""), "")
            .replace(Regex("""(?i)\bFetching Resource(\.\.\.)?\b"""), "")
            .replace('_', ' ')
            .trim(' ', '-', '•', '_', ':', '/', '\\', '(', ')', '[', ']')
    }

    fun downloadAndImport(url: String, userAgent: String?, contentDisposition: String?, mimeType: String?) {
        if (isDownloading) return
        isDownloading = true
        downloadStatus = "Downloading book into Veritas..."

        scope.launch(Dispatchers.IO) {
            var extractedTitle: String? = null
            val file = runCatching {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", userAgent ?: "Mozilla/5.0 (Linux; Android 14)")
                connection.setRequestProperty("Referer", "https://oceanofpdf.com/")
                val cookies = CookieManager.getInstance().getCookie(url)
                if (!cookies.isNullOrBlank()) {
                    connection.setRequestProperty("Cookie", cookies)
                }
                connection.connectTimeout = 20000
                connection.readTimeout = 35000
                connection.connect()

                val cdHeader = connection.getHeaderField("Content-Disposition") ?: contentDisposition
                if (!cdHeader.isNullOrBlank()) {
                    val match = Regex("filename[*]?=[\"']?([^\"';]+)[\"']?", RegexOption.IGNORE_CASE).find(cdHeader)
                    val rawName = match?.groupValues?.getOrNull(1)?.let {
                        runCatching { java.net.URLDecoder.decode(it, "UTF-8") }.getOrNull() ?: it
                    }
                    if (!rawName.isNullOrBlank()) {
                        val base = rawName.substringBeforeLast('.')
                        val clean = sanitizeBookTitle(base)
                        if (clean.isNotBlank()) {
                            extractedTitle = clean
                        }
                    }
                }

                val extension = when {
                    url.contains(".epub", ignoreCase = true) || mimeType?.contains("epub", ignoreCase = true) == true -> ".epub"
                    else -> ".pdf"
                }

                val finalTitle = extractedTitle?.takeIf { it.isNotBlank() }
                    ?: lastKnownBookTitle.takeIf { it.isNotBlank() }
                    ?: sanitizeBookTitle(pageTitle).takeIf { it.isNotBlank() }
                    ?: "OceanOfPdf_Book"

                extractedTitle = finalTitle
                val safeFileName = finalTitle.replace(Regex("[^a-zA-Z0-9._ -]"), "_").trim().ifBlank { "OceanOfPdf_Book" }
                val targetFile = File(context.cacheDir, "${safeFileName}$extension")

                connection.inputStream.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                targetFile
            }.getOrNull()

            withContext(Dispatchers.Main) {
                isDownloading = false
                downloadStatus = ""
                if (file != null && file.exists() && file.length() > 0) {
                    val titleToUse = extractedTitle ?: lastKnownBookTitle.ifBlank { "Imported Book" }
                    onImportDownloadedFile(file, titleToUse)
                    onDismiss()
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        BackHandler {
            if (webViewInstance?.canGoBack() == true) {
                webViewInstance?.goBack()
            } else {
                onDismiss()
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Navigation Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 4.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = {
                                if (webViewInstance?.canGoBack() == true) {
                                    webViewInstance?.goBack()
                                } else {
                                    onDismiss()
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }

                            IconButton(
                                onClick = { webViewInstance?.goForward() },
                                enabled = webViewInstance?.canGoForward() == true
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward")
                            }

                            IconButton(onClick = { webViewInstance?.reload() }) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Reload")
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp)
                            ) {
                                Text(
                                    text = pageTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentUrl,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Filled.Close, contentDescription = "Close")
                            }
                        }

                        if (isLoading && pageProgress in 0.01f..0.99f) {
                            LinearProgressIndicator(
                                progress = { pageProgress },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Downloading Banner
                if (isDownloading) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = downloadStatus,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // In-App WebView
                Box(modifier = Modifier.weight(1f)) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                webViewInstance = this
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    allowFileAccess = false
                                    allowContentAccess = false
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                                }

                                val cookieManager = CookieManager.getInstance()
                                cookieManager.setAcceptCookie(true)
                                cookieManager.setAcceptThirdPartyCookies(this, true)

                                setDownloadListener(DownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
                                    downloadAndImport(url, userAgent, contentDisposition, mimetype)
                                })

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        pageProgress = newProgress / 100f
                                        isLoading = newProgress < 100
                                    }

                                    override fun onReceivedTitle(view: WebView?, title: String?) {
                                        if (!title.isNullOrBlank()) {
                                            val clean = sanitizeBookTitle(title)
                                            if (clean.isNotBlank() && !clean.contains("Fetching Resource", ignoreCase = true) && !clean.contains("Ocean of PDF", ignoreCase = true)) {
                                                lastKnownBookTitle = clean
                                            }
                                            pageTitle = clean.ifBlank { "Ocean of PDF" }
                                        }
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        currentUrl = url ?: ""
                                        isLoading = true
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        currentUrl = url ?: ""
                                        isLoading = false
                                    }

                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        val reqUri = request?.url ?: return false
                                        val scheme = reqUri.scheme?.lowercase(java.util.Locale.ROOT)
                                        // Block non-web schemes for complete device security
                                        if (scheme != "http" && scheme != "https") {
                                            return true
                                        }
                                        val url = reqUri.toString()
                                        if (url.endsWith(".pdf", ignoreCase = true) ||
                                            url.endsWith(".epub", ignoreCase = true) ||
                                            url.contains("/download/", ignoreCase = true) ||
                                            url.contains("download_file", ignoreCase = true)
                                        ) {
                                            downloadAndImport(url, settings.userAgentString, null, null)
                                            return true
                                        }
                                        return false
                                    }
                                }

                                loadUrl(initialUrl)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}