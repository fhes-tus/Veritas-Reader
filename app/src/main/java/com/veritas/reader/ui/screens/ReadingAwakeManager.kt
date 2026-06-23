package com.veritas.reader.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

@Composable
fun KeepScreenAwake(
    enabled: Boolean,
    interactionTrigger: Long
) {
    if (!enabled) return

    val context = LocalContext.current
    val activity = remember(context) { context as? Activity }
    val window = remember(activity) { activity?.window }
    val lifecycleOwner = LocalLifecycleOwner.current

    var isTimerActive by remember { mutableStateOf(true) }

    LaunchedEffect(interactionTrigger, enabled) {
        isTimerActive = true
        // 20 minutes inactivity timeout (20 * 60 * 1000)
        delay(20L * 60L * 1000L)
        isTimerActive = false
    }

    DisposableEffect(lifecycleOwner, window, isTimerActive) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else if (event == Lifecycle.Event.ON_RESUME) {
                if (isTimerActive) {
                    window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        if (isTimerActive && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
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
