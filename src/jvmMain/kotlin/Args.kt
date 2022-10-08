import java.io.File

actual interface Args {
    actual fun getSource(): String {
        val filename = System.getenv("FILENAME")

        if (filename == null) {
            throw Exception("FILENAME environment variable not set")
        }

        return File(filename).readText()
    }

    actual fun getDebug(): Boolean {
        return System.getenv("DEBUG") == "1"
    }
}
