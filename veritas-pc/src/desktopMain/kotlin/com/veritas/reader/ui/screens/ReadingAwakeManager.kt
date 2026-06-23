package com.veritas.reader.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun KeepScreenAwake(
    enabled: Boolean,
    interactionTrigger: Long
) {
    // No-op on desktop
}

fun Modifier.monitorReadingActivity(
    onActivity: () -> Unit
): Modifier = this.pointerInput(Unit) {
    var lastUpdate = 0L
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent()
            val now = System.currentTimeMillis()
            if (now - lastUpdate > 1000L) {
                onActivity()
                lastUpdate = now
            }
        }
    }
}
