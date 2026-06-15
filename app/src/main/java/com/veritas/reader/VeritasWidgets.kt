package com.veritas.reader

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Paint.Style
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import java.io.File
import java.util.concurrent.Executors
import kotlin.math.max

// Cover rendering does file/PDF decoding; widget callbacks run on the main thread, so
// all cover work is funneled through this single background executor.
private val widgetExecutor = Executors.newSingleThreadExecutor()

class VeritasPlayerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { manager.updateAppWidget(it, playerViews(context)) }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, VeritasPlayerWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            ids.forEach { manager.updateAppWidget(it, playerViews(context)) }
        }

        private fun playerViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_veritas_player)
            val title = PlaybackStateStore.documentTitle.ifBlank { context.getString(R.string.app_name) }
            val progress = if (PlaybackStateStore.chunkCount > 0) {
                "Sentence ${PlaybackStateStore.currentIndex + 1} of ${PlaybackStateStore.chunkCount}"
            } else {
                PlaybackStateStore.statusMessage
            }
            views.setTextViewText(R.id.widget_title, title)
            views.setTextViewText(R.id.widget_subtitle, progress)
            views.setImageViewResource(
                R.id.widget_play_pause,
                if (PlaybackStateStore.isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
            )
            views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent(context, 10))
            views.setOnClickPendingIntent(R.id.widget_previous, servicePendingIntent(context, PlaybackActions.ACTION_PREVIOUS, 11))
            views.setOnClickPendingIntent(
                R.id.widget_play_pause,
                servicePendingIntent(context, if (PlaybackStateStore.isPlaying) PlaybackActions.ACTION_PAUSE else PlaybackActions.ACTION_PLAY, 12)
            )
            views.setOnClickPendingIntent(R.id.widget_next, servicePendingIntent(context, PlaybackActions.ACTION_NEXT, 13))
            return views
        }
    }
}

class VeritasCoverWidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_FAVORITE) {
            val result = goAsync()
            val appContext = context.applicationContext
            widgetExecutor.execute {
                try {
                    val repository = DocumentRepository(appContext)
                    val active = PlaybackStateStore.activeDocumentId?.let { repository.findDocument(it) }
                        ?: repository.loadDocuments().firstOrNull()
                    active?.let { repository.toggleFavorite(it.id) }
                    updateAllBlocking(appContext)
                } finally {
                    result.finish()
                }
            }
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val result = goAsync()
        val appContext = context.applicationContext
        widgetExecutor.execute {
            try {
                val views = coverViews(appContext)
                ids.forEach { manager.updateAppWidget(it, views) }
            } finally {
                result.finish()
            }
        }
    }

    companion object {
        private const val ACTION_TOGGLE_FAVORITE = "com.veritas.reader.widget.TOGGLE_FAVORITE"

        fun updateAll(context: Context) {
            val appContext = context.applicationContext
            val manager = AppWidgetManager.getInstance(appContext)
            val component = ComponentName(appContext, VeritasCoverWidgetProvider::class.java)
            if (manager.getAppWidgetIds(component).isEmpty()) return
            widgetExecutor.execute { runCatching { updateAllBlocking(appContext) } }
        }

        private fun updateAllBlocking(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, VeritasCoverWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isEmpty()) return
            val views = coverViews(context)
            ids.forEach { manager.updateAppWidget(it, views) }
        }

        // Playback updates refresh the widget every few seconds; cache the rendered
        // circle cover per document so it is decoded once, not per sentence.
        @Volatile
        private var cachedCircleCover: Pair<String, Bitmap>? = null

        private fun coverViews(context: Context): RemoteViews {
            val repository = DocumentRepository(context)
            val active = PlaybackStateStore.activeDocumentId?.let { repository.findDocument(it) }
                ?: repository.loadDocuments().firstOrNull()
            val views = RemoteViews(context.packageName, R.layout.widget_veritas_cover)
            views.setTextViewText(R.id.widget_cover_title, active?.title ?: context.getString(R.string.app_name))
            val cacheKey = active?.id ?: "fallback"
            val circleCover = cachedCircleCover?.takeIf { it.first == cacheKey }?.second
                ?: run {
                    val cover = active?.let { loadCoverBitmap(context, repository, it) }
                    if (cover != null) {
                        // Only real covers are cached; the fallback icon stays uncached so a
                        // cover extracted after the first render still shows up.
                        circleFitBitmap(cover).also { cachedCircleCover = cacheKey to it }
                    } else {
                        circleFitBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.veritas_reader_icon))
                    }
                }
            views.setImageViewBitmap(R.id.widget_cover_image, circleCover)
            views.setImageViewResource(
                R.id.widget_cover_play,
                if (PlaybackStateStore.isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
            )
            views.setTextViewText(R.id.widget_cover_favorite, if (active?.favorite == true) "♥" else "♡")
            views.setOnClickPendingIntent(R.id.widget_cover_root, mainPendingIntent(context, 20))
            views.setOnClickPendingIntent(
                R.id.widget_cover_play,
                servicePendingIntent(context, if (PlaybackStateStore.isPlaying) PlaybackActions.ACTION_PAUSE else PlaybackActions.ACTION_PLAY, 21)
            )
            views.setOnClickPendingIntent(R.id.widget_cover_favorite, favoritePendingIntent(context, 22))
            return views
        }

        private fun loadCoverBitmap(context: Context, repository: DocumentRepository, document: SavedDocument): Bitmap? {
            // 1. Try to load pre-extracted cover if it exists
            CoverExtractor.coverFile(context, document.id)?.let { file ->
                runCatching {
                    decodeFileBounded(file.absolutePath)
                }.getOrNull()?.let { return it }
            }

            // 2. Fallback to on-the-fly loading/rendering
            val original = repository.originalUri(document) ?: return null
            val mime = document.originalMimeType.lowercase()
            return when {
                mime.startsWith("image/") -> decodeStreamBounded(context, original)
                mime == "application/pdf" || document.originalFileName.endsWith(".pdf", ignoreCase = true) -> renderPdfFirstPage(context, original)
                else -> null
            }
        }

        // RemoteViews bitmaps are size-capped by the system; decode with inSampleSize so a
        // large source image cannot exhaust memory or get the widget update rejected.
        private const val MAX_COVER_DIMENSION = 1024

        private fun sampleSizeFor(width: Int, height: Int): Int {
            var sample = 1
            val maxSide = maxOf(width, height)
            while (maxSide / sample > MAX_COVER_DIMENSION) sample *= 2
            return sample
        }

        private fun decodeFileBounded(path: String): Bitmap? {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight)
            }
            return BitmapFactory.decodeFile(path, options)
        }

        private fun decodeStreamBounded(context: Context, uri: android.net.Uri): Bitmap? {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                ?: return null
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight)
            }
            return context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        }

        private fun renderPdfFirstPage(context: Context, uri: android.net.Uri): Bitmap? {
            return runCatching {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        if (renderer.pageCount <= 0) return@use null
                        renderer.openPage(0).use { page ->
                            val width = 512
                            val height = ((width.toFloat() / page.width.toFloat()) * page.height).toInt().coerceAtLeast(512)
                            createBitmap(width, height).also { bitmap ->
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            }
                        }
                    }
                }
            }.getOrNull()
        }

        private fun circleFitBitmap(source: Bitmap, targetSize: Int = 512): Bitmap {
            val output = createBitmap(targetSize, targetSize)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val path = Path().apply {
                addCircle(targetSize / 2f, targetSize / 2f, targetSize / 2f, Path.Direction.CW)
            }
            canvas.clipPath(path)
            canvas.drawColor(Color.rgb(18, 23, 27))

            val scale = max(
                targetSize.toFloat() / source.width.toFloat().coerceAtLeast(1f),
                targetSize.toFloat() / source.height.toFloat().coerceAtLeast(1f)
            )
            val width = source.width * scale
            val height = source.height * scale
            val left = (targetSize - width) / 2f
            val top = (targetSize - height) / 2f
            canvas.drawBitmap(source, null, RectF(left, top, left + width, top + height), paint)
            paint.style = Style.STROKE
            paint.strokeWidth = targetSize * 0.035f
            paint.color = Color.argb(230, 238, 238, 238)
            canvas.drawCircle(targetSize / 2f, targetSize / 2f, targetSize / 2f - paint.strokeWidth / 2f, paint)
            return output
        }
    }
}

private fun mainPendingIntent(context: Context, requestCode: Int): PendingIntent {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

private fun servicePendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
    val intent = Intent(context, PlaybackService::class.java).setAction(action)
    return PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

private fun favoritePendingIntent(context: Context, requestCode: Int): PendingIntent {
    val intent = Intent(context, VeritasCoverWidgetProvider::class.java).setAction("com.veritas.reader.widget.TOGGLE_FAVORITE")
    return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

fun updateVeritasWidgets(context: Context) {
    ContextCompat.getMainExecutor(context).execute {
        VeritasPlayerWidgetProvider.updateAll(context)
        VeritasCoverWidgetProvider.updateAll(context)
    }
}
