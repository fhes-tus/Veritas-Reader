package android.os

object Build {
    object VERSION {
        const val SDK_INT = 31
    }
    object VERSION_CODES {
        const val R = 30
        const val S = 31
        const val TIRAMISU = 33
    }
}

object Environment {
    fun getExternalStorageDirectory(): java.io.File {
        val userHome = System.getProperty("user.home")
        return java.io.File(userHome)
    }

    fun isExternalStorageManager(): Boolean = true
}
