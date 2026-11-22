package org.olcbox.app.ui.splash

import java.io.File

// java.io.tmpdir is the app cache dir on Android and the user temp dir on desktop. Losing the cache only
// means the bundled splash plays once until the next refresh.
internal actual object SplashDisk {
    private fun file(name: String) = File(File(System.getProperty("java.io.tmpdir"), "imba-splash"), name)

    actual fun read(name: String): ByteArray? = runCatching { file(name).takeIf { it.isFile }?.readBytes() }.getOrNull()

    actual fun write(name: String, bytes: ByteArray) {
        runCatching {
            val f = file(name)
            f.parentFile.mkdirs()
            val tmp = File(f.parentFile, "$name.tmp")
            tmp.writeBytes(bytes)
            if (!tmp.renameTo(f)) { f.delete(); tmp.renameTo(f) }      // never leave a half-written animation behind
        }
    }
}
