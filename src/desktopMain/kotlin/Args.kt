import platform.posix.*
import kotlinx.cinterop.*

actual interface Args {
    actual fun getSource(): String {
        val filePath = getenv("FILENAME")?.toKString() ?: throw Exception("FILENAME environment variable not set")

        val returnBuffer = StringBuilder()
        val file = fopen(filePath, "r") ?:
        throw IllegalArgumentException("Cannot open input file $filePath")

        try {
            memScoped {
                val readBufferLength = 64 * 1024
                val buffer = allocArray<ByteVar>(readBufferLength)
                var line = fgets(buffer, readBufferLength, file)?.toKString()
                while (line != null) {
                    returnBuffer.append(line)
                    line = fgets(buffer, readBufferLength, file)?.toKString()
                }
            }
        } finally {
            fclose(file)
        }

        return returnBuffer.toString()
    }

    actual fun getDebug(): Boolean {
        return getenv("DEBUG")?.toKString() == "1"
    }
}