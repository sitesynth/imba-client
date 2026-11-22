package org.olcbox.app.ui.splash

import androidx.compose.runtime.Composable

/** True when the user already signed in to the cabinet (then the splash skips the sign-in morph). */
@Composable
internal expect fun rememberCabinetSignedIn(): Boolean

/** Opens the sign-in screen the splash morphs into. */
@Composable
internal expect fun rememberOpenCabinet(): () -> Unit
