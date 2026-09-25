package com.klavs.bindle.data.entity.sealedclasses

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.klavs.bindle.R

sealed class MenuItem(
    val labelResource: Int,
    val icon: @Composable () -> Unit,
    val supportingContent: (@Composable ()-> Unit)? = null,
    val onClick: () -> Unit
) {
    data class Account(val onCLick: () -> Unit) : MenuItem(
        labelResource = R.string.my_account,
        icon = {
            Icon(
                imageVector = Icons.Rounded.PersonOutline,
                contentDescription = "account",
            )

        },
        supportingContent = {
            Text(stringResource(R.string.account_tab_supporting_content))
        },
        onClick = onCLick
    )
    data class AppSettings (val onCLick: () -> Unit): MenuItem(
        labelResource = R.string.app_settings,
        icon = { Icon(imageVector = Icons.Outlined.Settings, contentDescription = "App settings") },
        supportingContent = {
            Text(stringResource(R.string.app_settings_tab_supporting_content))
        },
        onClick = onCLick
    )

    data class Auth(val signedIn: Boolean, val onCLick: () -> Unit) : MenuItem(
        labelResource = if (signedIn) R.string.sign_out else R.string.sign_in,
        icon = {
            if (signedIn) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = "log out",
                    tint = MaterialTheme.colorScheme.error
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Login,
                    contentDescription = "log in",
                )
            }
        },
        onClick = onCLick
    )
    data class Theme(val onCLick: () -> Unit) : MenuItem(
        labelResource = R.string.theme,
        icon = { Icon(imageVector = Icons.Outlined.Palette, contentDescription = "Theme") },
        onClick = onCLick
    )
    data class Language(val onCLick: () -> Unit) : MenuItem(
        labelResource = R.string.language,
        icon = { Icon(imageVector = Icons.Outlined.Language, contentDescription = "Language") },
        onClick = onCLick
    )
    data class SupportAndFeedback(val onCLick: () -> Unit) : MenuItem(
        labelResource = R.string.support_and_feedback,
        icon = { Icon(imageVector = Icons.Outlined.Feedback, contentDescription = "support and feedback") },
        onClick = onCLick
    )
}