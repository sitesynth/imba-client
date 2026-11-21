package org.olcbox.app.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
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

// The site's TiledSwirlEngine palette (AnimatedImbaLogo.tsx).
private val SwirlPalette = listOf(
    Color(0xFFFFD731), Color(0xFF55DB9C), Color(0xFFC9A4FF), Color(0xFFFF7A3D), Color(0xFFFFFFFF)
)

private fun smoothStep(x: Float, e1: Float, e2: Float): Float {
    val t = ((x - e1) / (e2 - e1)).coerceIn(0f, 1f)
    return t * t * (3 - 2 * t)
}

/** The IMBA letters filled with the site's rotating tile swirl. */
@Composable
fun ImbaSwirlLogo(modifier: Modifier = Modifier) {
    val paths = remember { WORDMARK_PATHS.map { PathParser().parsePathString(it).toPath() } }
    val random = remember { Random(7) }
    val time by produceState(0f) {
        while (true) androidx.compose.runtime.withFrameNanos { value = it / 1_000_000_000f * 1.32f }
    }
    // Per-tile colours are fixed once per size so the swirl, not the colours, animates.
    val colors = remember { List(80 * 40) { SwirlPalette[random.nextInt(SwirlPalette.size)] } }
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
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
        // Hide everything outside the letters: ink everywhere except the letter shapes (even-odd cut-out).
        val k = h / VB_H
        val cutout = androidx.compose.ui.graphics.Path().apply {
            fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
            addRect(androidx.compose.ui.geometry.Rect(VB_X - 40f, VB_Y - 40f, VB_X + VB_W + 40f, VB_Y + VB_H + 40f))
            paths.forEach { addPath(it) }
        }
        scale(k, k, pivot = Offset.Zero) {
            translate(-VB_X, -VB_Y) { drawPath(cutout, ImbaInk) }
        }
    }
}

/** Full-screen launch splash: the animated logo on ink, fades away after [durationMs] (or on tap). */
@Composable
fun ImbaSplashOverlay(durationMs: Long = 1900) {
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
                .background(ImbaInk)
                .pointerInput(Unit) { detectTapGestures(onTap = { visible = false }) }
        ) {
            ImbaSwirlLogo(Modifier.size(width = 300.dp, height = 300.dp * (VB_H / VB_W)))
        }
    }
}
