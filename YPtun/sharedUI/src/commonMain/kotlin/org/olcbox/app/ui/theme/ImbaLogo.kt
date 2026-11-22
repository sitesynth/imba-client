package org.olcbox.app.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.tan
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Paths of public/imba-black.svg on the site (viewBox -4.3 147.8 1419.1 470.8).
internal const val VB_X = -4.3f
internal const val VB_Y = 147.8f
internal const val VB_W = 1419.1f
internal const val VB_H = 470.8f
internal val WORDMARK_PATHS = listOf(
    "M-2.3,147.8H233.1V618.6H-2.3Z",
    "M485.5,353.2c-2.6,28.2-30.1,27.9-32.4-.3l-18.9-203.3-168.5-1.5v470.4h113.7l1.9-263.8c.2-28.2,28-31,30.7-3l25.8,266.9h61.8l25.8-266.6c2.7-27.9,30.5-25.2,30.7,3l1.9,263.6h132.2V148.1l-185,1.5-19.5,203.6h-.2Z",
    "M1011.7,148.3h-289.2v469.9h289.2c22.4,0,40.6-27.6,40.6-61.6v-96.1c0-34-18.2-61.6-40.6-61.6h-53.5c-8.7-1.5-15.4-12.8-15.4-26.3s7.3-25.7,16.5-26.4h52.4c22.4,0,40.6-27.6,40.6-61.6v-74.4c0-34-18.2-61.6-40.6-61.6h0ZM901.3,509.8c0,14-7.5,25.4-16.7,25.4h-15.3c-9.2,0-16.7-11.4-16.7-25.4v-65.6c0-20.5,10.9-37,24.4-37s24.4,16.6,24.4,37v65.6h-.1ZM901.3,329.3c0,14-7.5,25.4-16.7,25.4h-15.3c-9.2,0-16.7-11.4-16.7-25.4v-60c0-20.5,10.9-37,24.4-37s24.4,16.6,24.4,37v60h-.1Z",
    "M1369.7,147.8h-240.5c-24.9,0-45.1,30.6-45.1,68.3v402.3h141v-95.3c0-14,7.5-25.4,16.7-25.4h15.3c9.2,0,16.7,11.4,16.7,25.4v95.3h141V216.1c0-37.7-20.2-68.3-45.1-68.3h0ZM1273.8,311.6v60c0,14-7.5,25.4-16.7,25.4h-15.3c-9.2,0-16.7-11.4-16.7-25.4v-88.2c0-20.5,10.9-37,24.4-37s24.4,16.6,24.4,37v28.1h-.1Z",
)

/** The bare IMBA wordmark, [height] tall, in [color]. Same vector as the site's /imba-black.svg. */
@Composable
fun ImbaWordmark(height: Dp, color: Color = ImbaInk, modifier: Modifier = Modifier) {
    val paths = remember { WORDMARK_PATHS.map { PathParser().parsePathString(it).toPath() } }
    val width = height * (VB_W / VB_H)
    Canvas(modifier = modifier.size(width, height)) {
        val k = size.height / VB_H
        scale(k, k, pivot = androidx.compose.ui.geometry.Offset.Zero) {
            translate(-VB_X, -VB_Y) { paths.forEach { drawPath(it, color) } }
        }
    }
}

/**
 * The site's logo lockup: yellow plate, 2dp ink border, hard 3dp shadow, black wordmark.
 * It is alive ([alive]): a constant small wiggle, now and then a jelly skew, and a pendulum sway as if the plate
 * hung on a nail at its top centre. Same 6 s timeline as the logo in the launch splash, at [amplitude] strength.
 */
@Composable
fun ImbaLogo(markHeight: Dp = 19.dp, modifier: Modifier = Modifier, alive: Boolean = true, amplitude: Float = 0.6f) {
    val shape = RoundedCornerShape(11.dp)
    val frame = if (alive) rememberLogoFrame() else null
    Box(
        modifier = modifier
            .then(if (frame != null) Modifier.logoAlive(frame, amplitude) else Modifier)
            .imbaHardShadow(shape, 3.dp)
            .clip(shape)
            .background(ImbaYellow)
            .border(2.dp, ImbaInk, shape)
            .padding(horizontal = markHeight * 0.62f, vertical = markHeight * 0.5f)
    ) {
        ImbaWordmark(height = markHeight)
    }
}

/** Frame 0..360 of a 6 s loop. Read inside a draw block only, so the logo redraws without recomposing. */
@Composable
private fun rememberLogoFrame(): State<Float> =
    rememberInfiniteTransition(label = "imba-logo").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "imba-logo-frame"
    )

// (frame, degrees): the jelly skew burst and the damped pendulum, as in the splash animation (60 fps frames).
private val LogoSkew = listOf(0f to 0f, 30f to 0f, 36f to -14f, 46f to 9f, 56f to -5f, 66f to 2f, 76f to 0f, 360f to 0f)
private val LogoSway = listOf(0f to 0f, 170f to 0f, 185f to 9f, 205f to -7f, 225f to 5f, 245f to -3f, 265f to 1.5f, 285f to 0f, 360f to 0f)

private fun keyed(frame: Float, points: List<Pair<Float, Float>>): Float {
    if (frame <= points.first().first) return points.first().second
    for (i in 1 until points.size) {
        val (f1, v1) = points[i]
        if (frame <= f1) {
            val (f0, v0) = points[i - 1]
            val t = (frame - f0) / (f1 - f0)
            return v0 + (v1 - v0) * (t * t * (3f - 2f * t))     // smoothstep between keys
        }
    }
    return points.last().second
}

private fun Modifier.logoAlive(frame: State<Float>, amplitude: Float): Modifier = drawWithContent {
    val f = frame.value
    val wiggle = 0.9f * sin(2.0 * PI * f / 120.0).toFloat()
    val breathe = 1f + 0.012f * amplitude * sin(2.0 * PI * f / 90.0 + 1.0).toFloat()
    val sway = keyed(f, LogoSway)
    val skew = keyed(f, LogoSkew)
    drawIntoCanvas { canvas ->
        canvas.save()
        canvas.translate(size.width / 2f, 0f)                      // the nail: top centre
        canvas.rotate((sway + wiggle) * amplitude)
        canvas.translate(-size.width / 2f, 0f)
        canvas.translate(size.width / 2f, size.height / 2f)        // breathing and jelly skew around the centre
        canvas.scale(breathe, breathe)
        canvas.skew(tan(skew * amplitude * PI / 180.0).toFloat(), 0f)
        canvas.translate(-size.width / 2f, -size.height / 2f)
        this@drawWithContent.drawContent()
        canvas.restore()
    }
}
