package androidx.work

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Data(val map: Map<String, Any>) {
    fun getString(key: String): String? = map[key] as? String
    fun getInt(key: String, defaultValue: Int): Int = (map[key] as? Number)?.toInt() ?: defaultValue
    fun getBoolean(key: String, defaultValue: Boolean): Boolean = map[key] as? Boolean ?: defaultValue
    fun getFloat(key: String, defaultValue: Float): Float = (map[key] as? Number)?.toFloat() ?: defaultValue
}

fun workDataOf(vararg pairs: Pair<String, Any?>): Data {
    val map = mutableMapOf<String, Any>()
    for ((k, v) in pairs) {
        if (v != null) map[k] = v
    }
    return Data(map)
}

class WorkInfo(
    val id: java.util.UUID,
    val state: State,
    val progress: Data,
    val outputData: Data
) {
    enum class State {
        ENQUEUED, RUNNING, SUCCEEDED, FAILED, CANCELLED;
        val isFinished: Boolean
            get() = this == SUCCEEDED || this == FAILED || this == CANCELLED
    }
}

open class WorkRequest(
    val id: java.util.UUID,
    val inputData: Data,
    val workerClass: Class<out ListenableWorker>
)

class OneTimeWorkRequest(
    id: java.util.UUID,
    inputData: Data,
    workerClass: Class<out ListenableWorker>
) : WorkRequest(id, inputData, workerClass)

class OneTimeWorkRequestBuilder<T : ListenableWorker>(private val workerClass: Class<T>) {
    private var inputData: Data = Data(emptyMap())
    private val id = java.util.UUID.randomUUID()

    fun setInputData(inputData: Data): OneTimeWorkRequestBuilder<T> {
        this.inputData = inputData
        return this
    }

    fun build(): OneTimeWorkRequest {
        return OneTimeWorkRequest(id, inputData, workerClass)
    }
}

inline fun <reified T : ListenableWorker> OneTimeWorkRequestBuilder(): OneTimeWorkRequestBuilder<T> {
    return OneTimeWorkRequestBuilder(T::class.java)
}

abstract class ListenableWorker(
    val appContext: Context,
    val workerParams: WorkerParameters
) {
    val inputData: Data
        get() = workerParams.inputData

    abstract suspend fun startWork(): Result

    suspend fun setProgress(data: Data) {
        workerParams.setProgress(data)
    }

    sealed class Result {
        class Success(val outputData: Data) : Result()
        class Failure(val outputData: Data) : Result()
        class Retry : Result()

        companion object {
            fun success(): Result = Success(Data(emptyMap()))
            fun success(outputData: Data): Result = Success(outputData)
            fun failure(): Result = Failure(Data(emptyMap()))
            fun failure(outputData: Data): Result = Failure(outputData)
            fun retry(): Result = Retry()
        }
    }
}

class WorkerParameters(
    val inputData: Data,
    private val onProgress: suspend (Data) -> Unit
) {
    suspend fun setProgress(data: Data) {
        onProgress(data)
    }
}

abstract class CoroutineWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : ListenableWorker(appContext, workerParams) {
    override suspend fun startWork(): Result {
        return doWork()
    }
    abstract suspend fun doWork(): Result
}

class WorkManager private constructor(val context: Context) {
    private val flows = mutableMapOf<java.util.UUID, MutableStateFlow<WorkInfo>>()

    fun enqueue(request: WorkRequest) {
        val flow = MutableStateFlow(
            WorkInfo(request.id, WorkInfo.State.ENQUEUED, Data(emptyMap()), Data(emptyMap()))
        )
        flows[request.id] = flow

        CoroutineScope(Dispatchers.IO).launch {
            try {
                flow.value = WorkInfo(request.id, WorkInfo.State.RUNNING, Data(emptyMap()), Data(emptyMap()))
                val params = WorkerParameters(request.inputData) { progressData ->
                    flow.value = WorkInfo(request.id, WorkInfo.State.RUNNING, progressData, Data(emptyMap()))
                }
                
                val constructor = request.workerClass.getConstructor(Context::class.java, WorkerParameters::class.java)
                val worker = constructor.newInstance(context, params)
                
                val result = worker.startWork()
                when (result) {
                    is ListenableWorker.Result.Success -> {
                        flow.value = WorkInfo(request.id, WorkInfo.State.SUCCEEDED, flow.value.progress, result.outputData)
                    }
                    is ListenableWorker.Result.Failure -> {
                        flow.value = WorkInfo(request.id, WorkInfo.State.FAILED, flow.value.progress, result.outputData)
                    }
                    else -> {
                        flow.value = WorkInfo(request.id, WorkInfo.State.FAILED, flow.value.progress, Data(emptyMap()))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                flow.value = WorkInfo(
                    request.id,
                    WorkInfo.State.FAILED,
                    flow.value.progress,
                    workDataOf("error" to (e.message ?: "Unknown background worker error"))
                )
            }
        }
    }

    fun getWorkInfoByIdFlow(id: java.util.UUID): Flow<WorkInfo?> {
        return flows[id] ?: MutableStateFlow(null)
    }

    companion object {
        private var instance: WorkManager? = null
        fun getInstance(context: Context): WorkManager {
            return instance ?: synchronized(this) {
                instance ?: WorkManager(context).also { instance = it }
            }
        }
    }
}
