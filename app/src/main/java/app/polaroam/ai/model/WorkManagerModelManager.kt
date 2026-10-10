package app.polaroam.ai.model

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import app.polaroam.core.coroutines.IoDispatcher
import app.polaroam.domain.model.ModelState
import app.polaroam.domain.repository.ModelManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

@Singleton
class WorkManagerModelManager @Inject constructor(
    private val workManager: WorkManager,
    private val files: ModelFiles,
    private val config: ModelConfig,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ModelManager {

    private val fileVersion = MutableStateFlow(0)

    override val state: Flow<ModelState> =
        if (!config.isConfigured) {
            flowOf(ModelState.NotConfigured)
        } else {
            combine(
                workManager.getWorkInfosForUniqueWorkFlow(ModelDownloadWorker.UNIQUE_WORK_NAME),
                fileVersion,
            ) { infos, _ -> resolve(infos.firstOrNull()) }
                .flowOn(io)
                .distinctUntilChanged()
        }

    override fun download() {
        if (!config.isConfigured) return
        val request = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .setRequiresStorageNotLow(true)
                    .build(),
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork(ModelDownloadWorker.UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }

    override fun cancel() {
        workManager.cancelUniqueWork(ModelDownloadWorker.UNIQUE_WORK_NAME)
    }

    override suspend fun delete() {
        cancel()
        withContext(io) { files.deleteAll() }
        fileVersion.update { it + 1 }
    }

    private fun resolve(info: WorkInfo?): ModelState = when (info?.state) {
        WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING, WorkInfo.State.BLOCKED -> {
            val progress = info.progress.getFloat(ModelDownloadWorker.KEY_PROGRESS, -1f)
            ModelState.Downloading(progress.takeIf { it >= 0f })
        }
        else -> when {
            files.readyModel() != null -> ModelState.Ready
            info?.state == WorkInfo.State.FAILED -> ModelState.Failed(
                info.outputData.getString(ModelDownloadWorker.KEY_ERROR) ?: "Download failed",
            )
            else -> ModelState.NotDownloaded
        }
    }

    private companion object {
        const val BACKOFF_SECONDS = 30L
    }
}
