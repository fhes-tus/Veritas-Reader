package com.veritas.reader

import java.io.File
import org.json.JSONObject

class DesktopPreferences {
    private val userHome = System.getProperty("user.home")
    private val dataDir = File(userHome, ".veritas_reader").apply { mkdirs() }
    private val prefsFile = File(dataDir, "settings.json")

    private val data = if (prefsFile.exists()) {
        runCatching { JSONObject(prefsFile.readText()) }.getOrDefault(JSONObject())
    } else {
        JSONObject()
    }

    @Synchronized
    fun getString(key: String, defValue: String?): String? {
        return if (data.has(key)) data.getString(key) else defValue
    }

    @Synchronized
    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return if (data.has(key)) data.getBoolean(key) else defValue
    }

    @Synchronized
    fun getInt(key: String, defValue: Int): Int {
        return if (data.has(key)) data.getInt(key) else defValue
    }

    @Synchronized
    fun getLong(key: String, defValue: Long): Long {
        return if (data.has(key)) data.getLong(key) else defValue
    }

    @Synchronized
    fun edit(action: DesktopPreferences.() -> Unit) {
        action()
    }

    fun edit(): DesktopPreferences = this

    @Synchronized
    fun remove(key: String): DesktopPreferences {
        data.remove(key)
        save()
        return this
    }

    @Synchronized
    fun putString(key: String, value: String?): DesktopPreferences {
        if (value == null) {
            data.remove(key)
        } else {
            data.put(key, value)
        }
        save()
        return this
    }

    @Synchronized
    fun putBoolean(key: String, value: Boolean): DesktopPreferences {
        data.put(key, value)
        save()
        return this
    }

    @Synchronized
    fun putInt(key: String, value: Int): DesktopPreferences {
        data.put(key, value)
        save()
        return this
    }

    @Synchronized
    fun putLong(key: String, value: Long): DesktopPreferences {
        data.put(key, value)
        save()
        return this
    }

    fun apply() {
        // Automatically persisted on change, so apply is a no-op
    }

    private fun save() {
        runCatching {
            prefsFile.writeText(data.toString(4))
        }
    }
}
