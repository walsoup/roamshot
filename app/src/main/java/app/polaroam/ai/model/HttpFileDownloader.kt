package app.polaroam.ai.model

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/** Downloads a URL into [target], resuming from the bytes already present when the server allows it. */
class HttpFileDownloader @Inject constructor() {

    suspend fun download(
        url: String,
        target: File,
        onProgress: suspend (downloadedBytes: Long, totalBytes: Long?) -> Unit,
    ) = withContext(Dispatchers.IO) {
        require(url.startsWith("https://", ignoreCase = true)) { "Only https downloads are allowed" }

        val existing = if (target.isFile) target.length() else 0L
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
            if (existing > 0) setRequestProperty("Range", "bytes=$existing-")
        }

        try {
            val append = when (val code = connection.responseCode) {
                HttpURLConnection.HTTP_PARTIAL -> true
                HttpURLConnection.HTTP_OK -> false
                HTTP_RANGE_NOT_SATISFIABLE -> {
                    target.delete()
                    throw IOException("Server rejected resume; restarting download")
                }
                else -> throw IOException("Download failed with HTTP $code")
            }

            val startAt = if (append) existing else 0L
            val total = connection.contentLengthLong.takeIf { it > 0 }?.plus(startAt)
            var downloaded = startAt
            onProgress(downloaded, total)

            connection.inputStream.use { input ->
                FileOutputStream(target, append).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        onProgress(downloaded, total)
                    }
                }
            }

            if (total != null && downloaded != total) {
                throw IOException("Download ended early ($downloaded of $total bytes)")
            }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 15_000
        const val READ_TIMEOUT_MS = 30_000
        const val BUFFER_SIZE = 64 * 1024
        const val HTTP_RANGE_NOT_SATISFIABLE = 416
    }
}
