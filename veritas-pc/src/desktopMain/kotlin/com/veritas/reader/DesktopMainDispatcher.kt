@file:OptIn(kotlinx.coroutines.InternalCoroutinesApi::class)
package com.veritas.reader

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.internal.MainDispatcherFactory
import javax.swing.SwingUtilities
import kotlin.coroutines.CoroutineContext

class DesktopMainDispatcher : MainCoroutineDispatcher() {
    override val immediate: MainCoroutineDispatcher
        get() = this

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            block.run()
        } else {
            SwingUtilities.invokeLater(block)
        }
    }

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return !SwingUtilities.isEventDispatchThread()
    }
}

class DesktopMainDispatcherFactory : MainDispatcherFactory {
    override val loadPriority: Int
        get() = 10000 // High priority to override any other factories

    override fun createDispatcher(allFactories: List<MainDispatcherFactory>): MainCoroutineDispatcher {
        return DesktopMainDispatcher()
    }
}
