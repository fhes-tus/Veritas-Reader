package android.content.pm

import android.content.Intent

class ResolveInfo {
    val serviceInfo: ServiceInfo? = null
    val activityInfo: ActivityInfo? = null
    fun loadLabel(pm: PackageManager): CharSequence = ""
}

class ServiceInfo {
    val packageName: String = ""
}

class ActivityInfo {
    val name: String = ""
}

class PackageInfo {
    val versionName: String = "1.0.1.1"
}

class PackageManager {
    fun queryIntentServices(intent: Intent, flags: Int): List<ResolveInfo> = emptyList()
    fun getPackageInfo(packageName: String, flags: Int): PackageInfo = PackageInfo()

    companion object {
        const val PERMISSION_GRANTED = 0
    }
}
