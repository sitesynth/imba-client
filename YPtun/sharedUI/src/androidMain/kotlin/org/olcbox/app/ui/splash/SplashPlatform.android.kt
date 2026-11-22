package org.olcbox.app.ui.splash

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** CabinetActivity keeps the site's sign-in token in these prefs once the user has signed in. */
@Composable
internal actual fun rememberCabinetSignedIn(): Boolean {
    val context = LocalContext.current
    return remember {
        runCatching {
            context.getSharedPreferences("imba_cabinet", Context.MODE_PRIVATE).getString("token", null) != null
        }.getOrDefault(false)
    }
}

@Composable
internal actual fun rememberOpenCabinet(): () -> Unit {
    val context = LocalContext.current
    return {
        runCatching {
            context.startActivity(Intent().setClassName(context.packageName, "org.olcbox.app.CabinetActivity"))
        }
    }
}
