package androidx.compose.ui.viewinterop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme

@Composable
fun <T : Any> AndroidView(
    factory: (android.content.Context) -> T,
    modifier: Modifier = Modifier,
    update: (T) -> Unit = {}
) {
    val context = android.content.Context()
    val view = remember { factory(context) }
    SideEffect {
        update(view)
    }

    if (view is android.widget.TextView) {
        val textStr = view.text?.toString().orEmpty()
        SelectionContainer(modifier = modifier) {
            Text(
                text = textStr,
                fontSize = if (view.textSize > 0) view.textSize.sp else 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
