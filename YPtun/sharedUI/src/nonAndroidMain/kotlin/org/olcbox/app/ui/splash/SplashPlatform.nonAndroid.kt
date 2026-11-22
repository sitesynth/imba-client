package org.olcbox.app.ui.splash

import androidx.compose.runtime.Composable

// The site cabinet is an Android screen; elsewhere the splash simply ends.
@Composable
internal actual fun rememberCabinetSignedIn(): Boolean = true

@Composable
internal actual fun rememberOpenCabinet(): () -> Unit = {}
