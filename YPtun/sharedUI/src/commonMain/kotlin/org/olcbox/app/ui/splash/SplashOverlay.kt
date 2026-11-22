package org.olcbox.app.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlin.time.Clock
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.olcbox.app.ui.theme.imbaDisplayFamily

private val SplashGround = Color(0xFF2E7DF6)        // the system splash colour, so the hand-over has no flash
private const val LOGO_ONLY_END_FRAME = 160f         // returning users: ~2.7 s of the logo, no sign-in morph
private var refreshStarted = false

/**
 * Launch splash: the site's animated hero (one Lottie, day or night by the clock) with the scrolling line on top.
 * Everything comes from the last downloaded splash config (see [SplashRemote]); tap skips it. For a user who is not
 * signed in the animation morphs into the sign-in screen, which is then opened.
 */
@OptIn(DelicateCoroutinesApi::class)
@Composable
internal fun SplashOverlay() {
    var visible by rememberSaveable { mutableStateOf(true) }
    var config by remember { mutableStateOf<SplashConfig?>(null) }
    val signedIn = rememberCabinetSignedIn()
    val openCabinet = rememberOpenCabinet()

    LaunchedEffect(Unit) {
        config = SplashRemote.config()
        if (!refreshStarted) {
            refreshStarted = true
            // must outlive the splash: the next launch plays what this download brings
            GlobalScope.launch(Dispatchers.Default) { SplashRemote.refresh() }
        }
    }

    AnimatedVisibility(visible = visible, exit = fadeOut(tween(350))) {
        Box(
            Modifier
                .fillMaxSize()
                .background(SplashGround)
                .pointerInput(Unit) { detectTapGestures(onTap = { visible = false }) }
        ) {
            config?.let { cfg ->
                SplashPlayer(cfg, signedIn) { openLogin ->
                    if (openLogin) openCabinet()
                    visible = false
                }
                SplashMarquee(cfg.marquee, Modifier.statusBarsPadding().padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun SplashPlayer(cfg: SplashConfig, signedIn: Boolean, onFinish: (openLogin: Boolean) -> Unit) {
    val night = remember(cfg) { isNight(cfg.theme) }
    val file = if (night) cfg.night.lottie else cfg.day.lottie
    val text by produceState<String?>(null, file, night) { value = SplashRemote.lottie(file, night) }
    val lottie = text ?: return

    val composition by rememberLottieComposition(night, lottie.length) {
        LottieCompositionSpec.JsonString(lottie)
    }
    val (fps, totalFrames) = remember(lottie) { frameInfo(lottie) }
    val progress = remember { Animatable(0f) }
    val morph = cfg.morph && !signedIn

    LaunchedEffect(composition) {
        if (composition == null) return@LaunchedEffect
        val endFrame = if (morph) totalFrames else minOf(LOGO_ONLY_END_FRAME, totalFrames)
        progress.animateTo(
            endFrame / totalFrames,
            tween(durationMillis = (endFrame / fps * 1000).toInt().coerceAtLeast(300), easing = LinearEasing)
        )
        onFinish(morph)
    }
    // a broken animation must not hold the app hostage behind a blue screen
    LaunchedEffect(composition) {
        delay(10_000)
        if (composition == null) onFinish(false)
    }

    Image(
        painter = rememberLottiePainter(composition, { progress.value }),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}

private fun isNight(theme: SplashThemeConfig): Boolean = when (theme.force) {
    "day" -> false
    "night" -> true
    else -> {
        val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
        hour >= theme.nightFromHour || hour < theme.dayFromHour
    }
}

/** Frame rate and length from the head of the Lottie file, so a longer or shorter remote design still plays fully. */
private fun frameInfo(lottie: String): Pair<Float, Float> {
    val head = lottie.take(600)
    val fps = Regex("\"fr\"\\s*:\\s*([0-9.]+)").find(head)?.groupValues?.get(1)?.toFloatOrNull() ?: 60f
    val op = Regex("\"op\"\\s*:\\s*([0-9.]+)").find(head)?.groupValues?.get(1)?.toFloatOrNull() ?: 440f
    return fps.coerceIn(1f, 120f) to op.coerceAtLeast(1f)
}

private fun colorOf(hex: String, fallback: Color): Color = runCatching {
    val h = hex.removePrefix("#")
    Color(if (h.length == 6) "FF$h".toLong(16) else h.toLong(16))
}.getOrDefault(fallback)

/** The site's scrolling line, drawn by the app from the config so its text can change without an update. */
@Composable
private fun SplashMarquee(m: SplashMarqueeConfig, modifier: Modifier = Modifier) {
    if (!m.enabled || m.items.isEmpty()) return
    val bg = colorOf(m.background, Color(0xFFE2CCFF))
    val fg = colorOf(m.text, Color(0xFF111111))
    val sep = colorOf(m.separatorColor, Color(0xFF7E6FB0))
    val family = imbaDisplayFamily()
    val speedPxPerS = with(LocalDensity.current) { m.speedDpPerS.coerceIn(10f, 400f).dp.toPx() }
    var unitPx by remember { mutableIntStateOf(0) }
    val shift = remember { Animatable(0f) }

    LaunchedEffect(unitPx, speedPxPerS) {
        if (unitPx <= 0) return@LaunchedEffect
        while (true) {
            shift.snapTo(0f)
            shift.animateTo(
                unitPx.toFloat(),
                tween((unitPx / speedPxPerS * 1000).toInt().coerceAtLeast(500), easing = LinearEasing)
            )
        }
    }

    Box(
        modifier.fillMaxWidth().height(38.dp).background(bg).clipToBounds(),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(Modifier.wrapContentWidth(align = Alignment.Start, unbounded = true).graphicsLayer { translationX = -shift.value }) {
            repeat(3) { copy ->           // one copy is measured, the others make the loop seamless
                Row(if (copy == 0) Modifier.onSizeChanged { unitPx = it.width } else Modifier) {
                    m.items.forEach { item ->
                        Text(
                            item, color = fg, fontFamily = family, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1,
                            softWrap = false, letterSpacing = (-0.14).sp, modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Text(
                            m.separator, color = sep, fontFamily = family, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1,
                            softWrap = false, modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
