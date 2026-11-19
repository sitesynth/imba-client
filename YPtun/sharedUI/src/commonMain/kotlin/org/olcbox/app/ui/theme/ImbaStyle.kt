package org.olcbox.app.ui.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The cabinet's "hard" offset shadow (box-shadow: 4px 4px 0 0 ink). Draw it BEFORE clip/background. */
fun Modifier.imbaHardShadow(
    shape: Shape = RoundedCornerShape(20.dp),
    offset: Dp = 4.dp,
    color: Color = ImbaInk
): Modifier = this.drawBehind {
    val o = offset.toPx()
    val outline = shape.createOutline(size, layoutDirection, this)
    translate(left = o, top = o) {
        when (outline) {
            is Outline.Rectangle -> drawRect(color, topLeft = outline.rect.topLeft, size = outline.rect.size)
            is Outline.Rounded -> drawPath(Path().apply { addRoundRect(outline.roundRect) }, color)
            is Outline.Generic -> drawPath(outline.path, color)
        }
    }
}

/** The cabinet ".panel": 2dp ink border plus the hard shadow. */
fun Modifier.imbaPanel(
    shape: Shape = RoundedCornerShape(20.dp),
    offset: Dp = 4.dp
): Modifier = this.imbaHardShadow(shape, offset).border(2.dp, ImbaInk, shape)
