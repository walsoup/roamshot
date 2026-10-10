package app.polaroam.ai.model

import java.io.File
import java.io.InputStream
import java.security.MessageDigest

object Sha256 {
    private const val BUFFER_SIZE = 64 * 1024

    fun hex(input: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(BUFFER_SIZE)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            digest.update(buffer, 0, read)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun hex(file: File): String = file.inputStream().use(::hex)

    fun matches(file: File, expectedHex: String): Boolean = hex(file).equals(expectedHex.trim(), ignoreCase = true)
}
