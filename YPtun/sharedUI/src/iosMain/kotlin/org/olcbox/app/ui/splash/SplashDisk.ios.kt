package org.olcbox.app.ui.splash

// No file cache here: the bundled splash plays and the refresh result is not kept.
internal actual object SplashDisk {
    actual fun read(name: String): ByteArray? = null
    actual fun write(name: String, bytes: ByteArray) = Unit
}
