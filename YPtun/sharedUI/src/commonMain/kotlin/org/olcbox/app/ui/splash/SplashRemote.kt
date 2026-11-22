package org.olcbox.app.ui.splash

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import multiplatform_app.sharedui.generated.resources.Res
import org.olcbox.app.data.datasource.createProxyHttpClient

/**
 * Splash settings published on the site (public/app/splash/config.json). The app plays the last copy it
 * downloaded, so the animation, its objects and the marquee text change without an app update.
 */
@Serializable
internal data class SplashConfig(
    val version: Int = 1,
    /** Play the morph into the sign-in screen when the user is not signed in. */
    val morph: Boolean = true,
    val marquee: SplashMarqueeConfig = SplashMarqueeConfig(),
    val theme: SplashThemeConfig = SplashThemeConfig(),
    val day: SplashLottieRef = SplashLottieRef("day.json"),
    val night: SplashLottieRef = SplashLottieRef("night.json"),
)

@Serializable
internal data class SplashMarqueeConfig(
    val enabled: Boolean = true,
    val items: List<String> = listOf("ZERO-LOG VPN", "VIRTUAL CARD", "NO BORDERS"),
    val separator: String = "✦",
    @SerialName("speed_dp_per_s") val speedDpPerS: Float = 75f,
    val background: String = "#E2CCFF",
    val text: String = "#111111",
    @SerialName("separator_color") val separatorColor: String = "#7E6FB0",
)

@Serializable
internal data class SplashThemeConfig(
    @SerialName("day_from_hour") val dayFromHour: Int = 6,
    @SerialName("night_from_hour") val nightFromHour: Int = 20,
    /** "day" / "night" pins the variant, anything else follows the clock. */
    val force: String? = null,
)

@Serializable
internal data class SplashLottieRef(val lottie: String)

/** Where downloaded splash files live. A miss only costs falling back to the bundled copy. */
internal expect object SplashDisk {
    fun read(name: String): ByteArray?
    fun write(name: String, bytes: ByteArray)
}

internal object SplashRemote {
    private const val BASE = "https://imba.live/app/splash/"
    private const val MAX_BYTES = 12_000_000
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /** Only plain file names from the config are fetched or written, never paths. */
    private fun safeName(name: String): String? =
        name.takeIf { it.length in 6..64 && it.endsWith(".json") && it.all { c -> c.isLetterOrDigit() || c == '.' || c == '-' || c == '_' } }

    private fun parse(text: String): SplashConfig? = runCatching { json.decodeFromString<SplashConfig>(text) }.getOrNull()

    private suspend fun bundled(name: String): ByteArray? = runCatching { Res.readBytes("files/splash/$name") }.getOrNull()

    /** The config to use right now: the last download, else the copy shipped in the app, else defaults. */
    suspend fun config(): SplashConfig =
        SplashDisk.read("config.json")?.decodeToString()?.let(::parse)
            ?: bundled("config.json")?.decodeToString()?.let(::parse)
            ?: SplashConfig()

    /** Lottie text for [file]: the last download, else the bundled day/night copy. */
    suspend fun lottie(file: String, night: Boolean): String? {
        val name = safeName(file)
        val text = name?.let { SplashDisk.read(it) }?.decodeToString()
        if (text != null && looksLikeLottie(text)) return text
        return bundled(if (night) "night.json" else "day.json")?.decodeToString()
    }

    private fun looksLikeLottie(text: String) = text.length > 1000 && text.trimStart().startsWith("{") && "\"layers\"" in text

    private sealed interface Fetched {
        class Body(val bytes: ByteArray) : Fetched
        data object Unchanged : Fetched
        data object Failed : Fetched
    }

    /** Downloads the config and the animations it points to; unchanged files answer 304 and cost nothing. */
    suspend fun refresh() {
        // the animations are a few MB: the default 8 s request limit of the shared client is too short for them
        val client = runCatching { createProxyHttpClient(requestTimeoutMs = 90_000, socketTimeoutMs = 45_000) }.getOrNull() ?: return
        try {
            val cfg = when (val r = fetch(client, "config.json")) {
                is Fetched.Body -> parse(r.bytes.decodeToString())?.also { SplashDisk.write("config.json", r.bytes) } ?: return
                Fetched.Unchanged -> config()                    // same config: the animations may still have changed
                Fetched.Failed -> return
            }
            for (ref in listOf(cfg.day, cfg.night).distinct()) {
                val name = safeName(ref.lottie) ?: continue
                val r = fetch(client, name) as? Fetched.Body ?: continue
                if (looksLikeLottie(r.bytes.decodeToString())) SplashDisk.write(name, r.bytes)
            }
        } catch (_: Throwable) {
            // offline or server hiccup: the cached copy keeps playing
        } finally {
            runCatching { client.close() }
        }
    }

    /** ETags are kept next to the files; a 304 is only trusted while the file itself is still on disk. */
    private suspend fun fetch(client: HttpClient, name: String): Fetched = runCatching {
        val etag = SplashDisk.read("$name.etag")?.decodeToString()
        val response = client.get(BASE + name) { if (etag != null && SplashDisk.read(name) != null) header(HttpHeaders.IfNoneMatch, etag) }
        if (response.status == HttpStatusCode.NotModified) return@runCatching Fetched.Unchanged
        if (!response.status.isSuccess()) return@runCatching Fetched.Failed
        val bytes = response.bodyAsBytes()
        if (bytes.isEmpty() || bytes.size > MAX_BYTES) return@runCatching Fetched.Failed
        response.headers[HttpHeaders.ETag]?.let { SplashDisk.write("$name.etag", it.encodeToByteArray()) }
        Fetched.Body(bytes)
    }.getOrDefault(Fetched.Failed)
}
