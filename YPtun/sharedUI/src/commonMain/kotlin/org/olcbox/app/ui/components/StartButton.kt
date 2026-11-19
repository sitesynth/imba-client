package org.olcbox.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.olcbox.app.ui.theme.ImbaGreen
import org.olcbox.app.ui.theme.ImbaInk
import org.olcbox.app.ui.theme.ImbaPaper
import org.olcbox.app.ui.theme.ImbaYellow
import org.olcbox.app.ui.theme.imbaHardShadow

sealed class StartButtonState {
    object Idle : StartButtonState()
    object Loading : StartButtonState()
    object Success : StartButtonState()
}

@Composable
fun StartButton(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    isLoading: Boolean,
    requiresSetup: Boolean = false,
    label: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val mainButtonColor by animateColorAsState(
        targetValue = when {
            isActive -> ImbaGreen
            isLoading -> ImbaPaper
            else -> ImbaYellow
        },
        label = "buttonColor"
    )
    val contentColor = ImbaInk

    // IMBA "pop" button: flat colour disc, 2.5dp ink border and a hard offset shadow.
    Box(
        modifier = modifier
            .size(132.dp)
            .imbaHardShadow(CircleShape, 5.dp)
            .clip(CircleShape)
            .background(mainButtonColor)
            .border(2.5.dp, ImbaInk, CircleShape)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(88.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = contentColor,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    imageVector = when {
                        isActive -> Icons.Rounded.Pause
                        requiresSetup -> Icons.Rounded.Add
                        else -> Icons.Rounded.PowerSettingsNew
                    },
                    contentDescription = label ?: "Toggle",
                    tint = contentColor.copy(alpha = if (!enabled) 0.5f else 1f),
                    modifier = Modifier.size(44.dp)
                )
            }
        }
    }
}
