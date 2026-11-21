package org.olcbox.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * IMBA cabinet inside the app: the site's own pages (sign-in, balance, traffic, support) in a WebView.
 *
 *  - Sign-in is the site's login form. After it, the `imba_token` cookie is read and the account's
 *    subscription link is fetched from the API and imported through the existing `yptun://import/` link.
 *  - Top-up pages open in the system browser (Chrome) through a one-time sign-in link: Google Pay and saved
 *    cards do not work inside a WebView.
 */
class CabinetActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var progress: ProgressBar
    private val prefs by lazy { getSharedPreferences("imba_cabinet", Context.MODE_PRIVATE) }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ink = Color.parseColor("#111111")
        val yellow = Color.parseColor("#FFD731")
        val dp = resources.displayMetrics.density

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(yellow)
            setPadding((12 * dp).toInt(), (10 * dp).toInt(), (12 * dp).toInt(), (10 * dp).toInt())
        }
        val close = TextView(this).apply {
            text = "←"
            textSize = 24f
            setTextColor(ink)
            setPadding((8 * dp).toInt(), 0, (16 * dp).toInt(), 0)
            setOnClickListener { finish() }
        }
        val title = TextView(this).apply {
            text = "Кабинет IMBA"
            textSize = 18f
            setTextColor(ink)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        bar.addView(close)
        bar.addView(title)

        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            visibility = View.GONE
        }
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            setBackgroundColor(Color.WHITE)
            CookieManager.getInstance().setAcceptCookie(true)
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                    route(request.url)

                override fun onPageFinished(view: WebView, url: String) {
                    progress.visibility = View.GONE
                    syncAccount()
                }

                override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                    progress.visibility = View.VISIBLE
                }
            }
        }

        val root = FrameLayout(this)
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        column.addView(bar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        column.addView(progress, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (3 * dp).toInt()))
        column.addView(web, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(column)
        setContentView(root)

        val start = intent?.getStringExtra(EXTRA_PATH)?.takeIf { it.startsWith("/") } ?: "/dashboard"
        if (savedInstanceState == null) web.loadUrl("$SITE$start") else web.restoreState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    /** true = handled outside the WebView. */
    private fun route(uri: Uri): Boolean {
        val host = uri.host.orEmpty()
        val onSite = host == "imba.live" || host.endsWith(".imba.live")
        if (!onSite) {
            // Payment providers, Telegram, mailto: real apps/Chrome do these properly (Google Pay works there).
            return runCatching { startActivity(Intent(Intent.ACTION_VIEW, uri)); true }.getOrDefault(false)
        }
        val path = uri.path.orEmpty()
        if (path.startsWith("/dashboard/billing/topup") && !path.endsWith("/result")) {
            openInBrowserSignedIn(path)
            return true
        }
        return false
    }

    /** Asks the API for a one-time sign-in link and opens it in Chrome; falls back to the WebView. */
    private fun openInBrowserSignedIn(path: String) {
        val token = prefs.getString("token", null)
        if (token == null) { web.loadUrl("$SITE$path"); return }
        thread {
            val url = runCatching {
                val c = (URL("$API/v1/auth/app-handoff").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 10_000; readTimeout = 10_000
                    doOutput = true
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "application/json")
                }
                c.outputStream.use { it.write("""{"next":"$path"}""".toByteArray()) }
                if (c.responseCode != 200) null
                else org.json.JSONObject(c.inputStream.bufferedReader().readText()).optString("url").takeIf { it.isNotBlank() }
            }.getOrNull()
            runOnUiThread {
                if (url != null) startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                else web.loadUrl("$SITE$path")
            }
        }
    }

    /** After sign-in: remember the token and add the account's subscription to the app, once per link. */
    private fun syncAccount() {
        val cookies = CookieManager.getInstance().getCookie(SITE).orEmpty()
        val token = cookies.split(";").map { it.trim() }
            .firstOrNull { it.startsWith("imba_token=") }?.removePrefix("imba_token=")?.takeIf { it.isNotBlank() }
        if (token == null) { prefs.edit().remove("token").apply(); return }
        if (prefs.getString("token", null) == token) return
        prefs.edit().putString("token", token).apply()
        thread {
            val links = runCatching {
                val c = (URL("$API/v1/me/vpn").openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10_000; readTimeout = 10_000
                    setRequestProperty("Authorization", "Bearer $token")
                }
                if (c.responseCode != 200) return@runCatching emptyList<String>()
                val arr = JSONArray(c.inputStream.bufferedReader().readText())
                (0 until arr.length()).mapNotNull { arr.getJSONObject(it).optString("server_key").takeIf { k -> k.startsWith("http") } }
            }.getOrDefault(emptyList())
            val imported = prefs.getStringSet("imported", emptySet()).orEmpty()
            val fresh = links.filter { it !in imported }.distinct()
            if (fresh.isEmpty()) return@thread
            prefs.edit().putStringSet("imported", imported + fresh).apply()
            runOnUiThread {
                fresh.forEach { link ->
                    startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("yptun://import/" + Uri.encode(link))).setPackage(packageName)
                    )
                }
                Toast.makeText(this, "Подписка добавлена в приложение", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val EXTRA_PATH = "path"
        private const val SITE = "https://imba.live"
        private const val API = "https://api.imba.live"
    }
}
