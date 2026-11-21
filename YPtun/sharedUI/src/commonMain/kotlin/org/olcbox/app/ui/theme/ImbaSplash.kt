package org.olcbox.app.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlinx.coroutines.delay

// Port of the site's hero logo (src/app/page.tsx): blue radial ground, RainbowImbaLogo (white letters with sliding
// light beams) under AnimatedImbaLogo (tile swirl, opacity 0.7), hard drop shadows under the letters.

private val SwirlPalette = listOf(
    Color(0xFFFFD731), Color(0xFF55DB9C), Color(0xFFC9A4FF), Color(0xFFFF7A3D), Color(0xFFFFFFFF)
)

private val BeamPurple = Color(0xFFE879F9)
private val BeamBlue = Color(0xFF60A5FA)
private val BeamGreen = Color(0xFF5EEAD4)
private val BeamYellow = Color(0xFFFACC15)
private val BeamCombos = listOf(
    listOf(BeamPurple, BeamBlue, BeamGreen),
    listOf(BeamPurple, BeamGreen, BeamBlue),
    listOf(BeamGreen, BeamPurple, BeamBlue),
    listOf(BeamGreen, BeamBlue, BeamPurple),
    listOf(BeamBlue, BeamGreen, BeamPurple),
    listOf(BeamBlue, BeamPurple, BeamGreen),
)
private const val BEAM_COUNT = 25
private const val BEAM_ANIM_S = 45f

private class BeamBand(val dx: Float, val blur: Float, val spread: Float, val color: Color)

private fun smoothStep(x: Float, e1: Float, e2: Float): Float {
    val t = ((x - e1) / (e2 - e1)).coerceIn(0f, 1f)
    return t * t * (3 - 2 * t)
}

private fun DrawScope.drawLetters(paths: List<Path>, k: Float, color: Color) {
    scale(k, k, pivot = Offset.Zero) {
        translate(-VB_X, -VB_Y) { paths.forEach { drawPath(it, color) } }
    }
}

/** Erases everything outside the letter shapes (the layer must be Offscreen). */
private fun DrawScope.clearOutsideLetters(cutout: Path) {
    val k = size.height / VB_H
    scale(k, k, pivot = Offset.Zero) {
        translate(-VB_X, -VB_Y) { drawPath(cutout, Color.Black, blendMode = BlendMode.Clear) }
    }
}

private fun DrawScope.drawBand(x0: Float, band: BeamBand, dp: Float, height: Float) {
    val half = (band.spread + band.blur / 2f) * dp
    val width = half * 2f
    val left = x0 + band.dx * dp - half
    val edge = (band.blur / 2f * dp) / width
    val brush = Brush.horizontalGradient(
        0f to band.color.copy(alpha = 0f),
        edge to band.color,
        1f - edge to band.color,
        1f to band.color.copy(alpha = 0f),
        startX = left,
        endX = left + width
    )
    drawRect(brush, topLeft = Offset(left, 0f), size = Size(width, height * 2f))
}

/** RainbowImbaLogo: white ground, 25 tilted beams (yellow / purple / blue / green / white glow) sliding across. */
private fun DrawScope.drawRainbow(t: Float) {
    val w = size.width
    val h = size.height
    val dp = 1.dp.toPx()
    drawRect(Color.White)
    val vw = w / 0.74f // the site logo is 74vw wide; the beams travel in vw
    for (i in 0 until BEAM_COUNT) {
        val duration = BEAM_ANIM_S - (BEAM_ANIM_S / BEAM_COUNT / 2f) * (i + 1)
        val shift = (i + 1f) / BEAM_COUNT * BEAM_ANIM_S
        val p = ((t + shift) / duration) % 1f
        val x0 = w - (-0.25f * vw + p * 1.5f * vw)
        val c = BeamCombos[i % BeamCombos.size]
        // CSS paints the first box-shadow on top, so draw the last one first.
        val bands = listOf(
            BeamBand(130f, 80f, 40f, Color.White),
            BeamBand(50f, 50f, 25f, c[2]),
            BeamBand(0f, 50f, 25f, c[1]),
            BeamBand(-50f, 50f, 25f, c[0]),
            BeamBand(-130f, 80f, 40f, BeamYellow),
        )
        rotate(10f, pivot = Offset(x0, 0f)) { bands.forEach { drawBand(x0, it, dp, h) } }
    }
}

/** AnimatedImbaLogo: the site's TiledSwirlEngine. */
private fun DrawScope.drawSwirl(t: Float, colors: List<Color>) {
    val w = size.width
    val h = size.height
    val time = t * 1.32f
    val tile = max(1f, min(w, h) / 18f)
    val cols = ceil(w / tile).toInt() + 1
    val rows = ceil(h / tile).toInt() + 1
    val cx = w / 2
    val cy = h / 2
    val distMax = sqrt(cx * cx + cy * cy)
    for (i in 0 until cols) for (j in 0 until rows) {
        val x = i * tile
        val y = j * tile
        val dx = x + tile / 2 - cx
        val dy = y + tile / 2 - cy
        val distN = sqrt(dx * dx + dy * dy) / distMax
        val ripple = 2.8f * (1 + distN * 4f)
        val wave = sin(distN * ripple + atan2(dy, dx) - time)
        val ts = tile * 0.15f + tile * 0.85f * smoothStep((wave + 1) / 2, 0.38f, 0.62f)
        drawRect(
            colors[(i * 40 + j) % colors.size],
            topLeft = Offset(x + (tile - ts) / 2, y + (tile - ts) / 2),
            size = Size(ts, ts)
        )
    }
}

/** The hero logo: drop shadows, rainbow letters, tile swirl on top at 70%. */
@Composable
fun ImbaSwirlLogo(modifier: Modifier = Modifier) {
    val paths = remember { WORDMARK_PATHS.map { PathParser().parsePathString(it).toPath() } }
    val cutout = remember {
        Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(VB_X - 40f, VB_Y - 40f, VB_X + VB_W + 40f, VB_Y + VB_H + 40f))
            paths.forEach { addPath(it) }
        }
    }
    val colors = remember {
        val random = Random(7)
        List(80 * 40) { SwirlPalette[random.nextInt(SwirlPalette.size)] }
    }
    val t by produceState(0f) {
        while (true) androidx.compose.runtime.withFrameNanos { value = it / 1_000_000_000f }
    }
    Box(modifier) {
        Canvas(Modifier.matchParentSize()) {
            val k = size.height / VB_H
            translate(0f, 6.dp.toPx()) { drawLetters(paths, k, Color.Black.copy(alpha = 0.55f)) }
            translate(3.dp.toPx(), 3.dp.toPx()) { drawLetters(paths, k, Color.Black.copy(alpha = 0.35f)) }
        }
        Canvas(Modifier.matchParentSize().graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)) {
            drawRainbow(t)
            clearOutsideLetters(cutout)
        }
        Canvas(
            Modifier.matchParentSize().graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen, alpha = 0.7f)
        ) {
            drawSwirl(t, colors)
            clearOutsideLetters(cutout)
        }
    }
}

/** Full-screen launch splash: the site's blue hero with the animated logo; fades after [durationMs] or on tap. */
@Composable
fun ImbaSplashOverlay(durationMs: Long = 2200) {
    var visible by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(durationMs)
        visible = false
    }
    AnimatedVisibility(visible = visible, exit = fadeOut(tween(350))) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            0f to Color(0xFF5AA0FF),
                            0.45f to Color(0xFF2E7DF6),
                            1f to Color(0xFF1452C9),
                            center = Offset(size.width * 0.5f, size.height * 0.35f),
                            radius = size.height * 0.9f
                        )
                    )
                }
                .pointerInput(Unit) { detectTapGestures(onTap = { visible = false }) }
        ) {
            ImbaSwirlLogo(Modifier.fillMaxWidth(0.74f).aspectRatio(VB_W / VB_H))
        }
    }
}
