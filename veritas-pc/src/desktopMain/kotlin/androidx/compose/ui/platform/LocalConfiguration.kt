package androidx.compose.ui.platform

import java.util.Locale

class Configuration {
    val screenWidthDp: Int = 1024
    val screenHeightDp: Int = 768
    val locales: List<Locale> = listOf(Locale.getDefault())
}

object LocalConfiguration {
    val current: Configuration
        @androidx.compose.runtime.Composable
        get() = Configuration()
}
