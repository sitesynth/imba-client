package org.olcbox.app.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
private const val VB_X = -4.3f
private const val VB_Y = 147.8f
private const val VB_W = 1419.1f
private const val VB_H = 470.8f
private val WORDMARK_PATHS = listOf(
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

/** The site's logo lockup: yellow plate, 2dp ink border, hard 3dp shadow, black wordmark. */
@Composable
fun ImbaLogo(markHeight: Dp = 19.dp, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier = modifier
            .imbaHardShadow(shape, 3.dp)
            .clip(shape)
            .background(ImbaYellow)
            .border(2.dp, ImbaInk, shape)
            .padding(horizontal = markHeight * 0.62f, vertical = markHeight * 0.5f)
    ) {
        ImbaWordmark(height = markHeight)
    }
}
