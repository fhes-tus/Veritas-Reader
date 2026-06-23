package androidx.lifecycle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import android.app.Application

open class ViewModel

val ViewModel.viewModelScope: CoroutineScope
    get() = CoroutineScope(SupervisorJob() + Dispatchers.Main)

open class AndroidViewModel(application: Application) : ViewModel() {
    private val _application = application
    fun <T : Application> getApplication(): T = _application as T
}
