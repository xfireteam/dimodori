package com.dimodori.app.tv.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dimodori.app.tv.ui.screens.detail.TrackSelectorActivation
import com.dimodori.app.tv.ui.screens.detail.trackSelectorActivation
import com.jellycine.shared.R

/** A sibling control is reachable outside the text editor's internal focus group. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TvPasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    enabled: Boolean,
    nextFocusRequester: FocusRequester = FocusRequester.Default,
    modifier: Modifier = Modifier,
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var eyeFocused by remember { mutableStateOf(false) }
    val passwordFocus = remember { FocusRequester() }
    val eyeFocus = remember { FocusRequester() }
    val navigationPress = remember { TrackSelectorActivation() }
    val keyboard = LocalSoftwareKeyboardController.current
    val imeVisible = WindowInsets.isImeVisible

    LaunchedEffect(enabled, imeVisible) { navigationPress.reset() }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Keep this editor at the same composition position. The String overload
        // retains its internal selection/composition when only transformation changes.
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.password)) },
            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.weight(1f)
                .focusRequester(passwordFocus)
                .focusProperties {
                    right = eyeFocus
                    down = eyeFocus
                    next = eyeFocus
                }
                .onFocusChanged { if (!it.isFocused) navigationPress.reset() }
                .onPreviewKeyEvent { event ->
                    // While the IME is open, leave its navigation/editing untouched.
                    // After Back closes it, Right or Down reaches the eye on release.
                    if (enabled && !imeVisible &&
                        (event.key == Key.DirectionRight || event.key == Key.DirectionDown)
                    ) {
                        when (event.type) {
                            KeyEventType.KeyDown -> navigationPress.keyDown(
                                event.nativeKeyEvent.keyCode, event.nativeKeyEvent.repeatCount
                            )
                            KeyEventType.KeyUp -> if (navigationPress.keyUp(
                                    event.nativeKeyEvent.keyCode, event.nativeKeyEvent.isCanceled
                                )) eyeFocus.requestFocus()
                        }
                        true
                    } else false
                },
            enabled = enabled,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = tvTextFieldColors(),
        )
        IconButton(
            onClick = { isPasswordVisible = !isPasswordVisible },
            enabled = enabled,
            modifier = Modifier.size(48.dp)
                .focusRequester(eyeFocus)
                .focusProperties {
                    left = passwordFocus
                    up = passwordFocus
                    previous = passwordFocus
                    down = nextFocusRequester
                    next = nextFocusRequester
                }
                .onFocusChanged {
                    eyeFocused = it.isFocused
                    if (it.isFocused) keyboard?.hide()
                }
                .trackSelectorActivation {
                    if (enabled) isPasswordVisible = !isPasswordVisible
                },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (eyeFocused) Color.White else Color.White.copy(alpha = 0.12f),
                contentColor = if (eyeFocused) Color.Black else Color.White,
                disabledContainerColor = Color.White.copy(alpha = 0.05f),
                disabledContentColor = Color.White.copy(alpha = 0.38f),
            ),
        ) {
            Icon(
                imageVector = if (isPasswordVisible) Icons.Rounded.VisibilityOff
                    else Icons.Rounded.Visibility,
                contentDescription = stringResource(
                    if (isPasswordVisible) R.string.auth_hide_password else R.string.auth_show_password
                ),
            )
        }
    }
}
