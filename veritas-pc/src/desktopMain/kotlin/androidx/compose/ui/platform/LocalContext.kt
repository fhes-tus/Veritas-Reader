package androidx.compose.ui.platform

import android.content.Context
import androidx.compose.runtime.Composable

object LocalContext {
    private val stubContext = Context()

    val current: Context
        @Composable
        get() = stubContext
}
