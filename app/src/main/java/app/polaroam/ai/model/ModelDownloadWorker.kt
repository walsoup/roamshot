package app.polaroam.ai.model

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import app.polaroam.R
import app.polaroam.core.result.runCatchingCancellable
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class ModelDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val config: ModelConfig,
    private val files: ModelFiles,
    private val downloader: HttpFileDownloader,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!config.isConfigured) return failure("No model URL is configured")

        runCatchingCancellable { setForeground(foregroundInfo(percent = null)) }

        val choiceId = inputData.getString(KEY_MODEL_CHOICE)
        val choice = QuestModelChoice.fromId(choiceId)
        val specsToDownload = config.packFor(choice)
        val totalBytes = specsToDownload.sumOf { it.sizeBytes }.coerceAtLeast(1L)
        var finishedBytes = 0L
        var lastPercent = -1

        return try {
            for (spec in specsToDownload) {
                if (withContext(Dispatchers.IO) { files.readyModel(spec) } == null) {
                    val part = withContext(Dispatchers.IO) { files.preparePartFile(spec) }
                    downloader.download(spec.url, part) { downloaded, _ ->
                        val percent = (((finishedBytes + downloaded) * 100) / totalBytes).toInt().coerceIn(0, 100)
                        if (percent != lastPercent) {
                            lastPercent = percent
                            setProgress(workDataOf(KEY_PROGRESS to percent / 100f))
                            runCatchingCancellable { setForeground(foregroundInfo(percent)) }
                        }
                    }
                    verifyAndPromote(spec)?.let { return failure(it) }
                }
                finishedBytes += spec.sizeBytes
            }
            Result.success()
        } catch (e: IOException) {
            if (runAttemptCount < MAX_RETRIES) Result.retry() else failure(e.message ?: "Network error")
        } catch (e: IllegalArgumentException) {
            failure(e.message ?: "Invalid download configuration")
        }
    }

    /** Returns a failure reason, or null once the file is checked and in place. */
    private suspend fun verifyAndPromote(spec: ModelSpec): String? = withContext(Dispatchers.IO) {
        val expected = spec.expectedSha256
        if (expected != null && !Sha256.matches(files.partFile(spec), expected)) {
            files.discardPart(spec)
            return@withContext "${spec.fileName} failed its checksum"
        }
        files.promotePart(spec)
        null
    }

    private fun failure(reason: String): Result = Result.failure(workDataOf(KEY_ERROR to reason))

    private fun foregroundInfo(percent: Int?): ForegroundInfo {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    applicationContext.getString(R.string.notification_channel_downloads),
                    NotificationManager.IMPORTANCE_LOW,
                ),
            )
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(applicationContext.getString(R.string.notification_downloading_model))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, percent ?: 0, percent == null)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "polaroam-model-download"
        const val KEY_PROGRESS = "progress"
        const val KEY_ERROR = "error"
        const val KEY_MODEL_CHOICE = "model_choice"
        private const val CHANNEL_ID = "model-downloads"
        private const val NOTIFICATION_ID = 4101
        private const val MAX_RETRIES = 3
    }
}
