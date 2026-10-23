package com.dimodori.app.ui.screens.auth

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.jellycine.shared.R
import com.jellycine.shared.ui.theme.JellyBlue

internal const val ConnectionSupportUrl = "https://wa.me/17869365291"

/** Independent of connection/loading state so help is available before signing in. */
@Composable
internal fun ConnectionSupportLink() {
    val context = LocalContext.current
    TextButton(
        onClick = { openConnectionSupport(context) },
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = JellyBlue),
    ) {
        Text(
            text = stringResource(R.string.auth_support_link),
            style = MaterialTheme.typography.bodyMedium,
            textDecoration = TextDecoration.Underline,
        )
    }
}

internal fun openConnectionSupport(context: Context) {
    try {
        // An ordinary HTTPS link allows WhatsApp or a browser; never pin a package.
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(ConnectionSupportUrl))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, R.string.auth_support_open_failed, Toast.LENGTH_LONG).show()
    } catch (_: SecurityException) {
        Toast.makeText(context, R.string.auth_support_open_failed, Toast.LENGTH_LONG).show()
    }
}
