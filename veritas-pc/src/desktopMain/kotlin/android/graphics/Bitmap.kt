package android.graphics

import java.io.File
import org.jetbrains.skia.Image

class Bitmap(val width: Int, val height: Int, val skiaImage: Image)

class RectF(val left: Float, val top: Float, val right: Float, val bottom: Float)

object BitmapFactory {
    fun decodeFile(path: String): Bitmap? {
        return runCatching {
            val file = File(path)
            if (!file.exists()) return null
            val bytes = file.readBytes()
            val skiaImage = Image.makeFromEncoded(bytes)
            Bitmap(skiaImage.width, skiaImage.height, skiaImage)
        }.getOrNull()
    }
}
