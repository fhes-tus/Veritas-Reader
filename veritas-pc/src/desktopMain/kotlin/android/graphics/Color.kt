package android.graphics

object Color {
    fun parseColor(colorString: String): Int {
        return try {
            if (colorString.startsWith("#")) {
                var color = colorString.substring(1).toLong(16)
                if (colorString.length == 7) {
                    color = color or 0xFF000000L
                }
                color.toInt()
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    fun colorToHSV(color: Int, hsv: FloatArray) {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        val hsb = java.awt.Color.RGBtoHSB(r, g, b, null)
        hsv[0] = hsb[0] * 360f
        hsv[1] = hsb[1]
        hsv[2] = hsb[2]
    }

    fun HSVToColor(hsv: FloatArray): Int {
        return java.awt.Color.HSBtoRGB(hsv[0] / 360f, hsv[1], hsv[2])
    }
}
