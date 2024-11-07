package com.klavs.bindle.data.entity

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.klavs.bindle.R

sealed class MenuItem(
    val label: String,
    val icon: @Composable () -> Unit,
    val onClick: () -> Unit
) {
    data class Profile(val onCLick: () -> Unit) : MenuItem(
        label = "Profile",
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.rounded_person_24),
                contentDescription = "log out",
                modifier = Modifier.size(30.dp),
            )

        },
        onClick = onCLick
    )
    data class AppSettings (val onCLick: () -> Unit):MenuItem(
        label = "App Settings",
        icon = { Icon(imageVector = Icons.Outlined.Settings, contentDescription = "App settings") },
        onClick = onCLick
    )

    data class Auth(val signedIn: Boolean, val onCLick: () -> Unit) : MenuItem(
        label = if (signedIn) "Log out" else "Log in",
        icon = {
            if (signedIn) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = "log out",
                    modifier = Modifier.size(30.dp),
                    tint = Color.Red
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Login,
                    contentDescription = "log in",
                    modifier = Modifier.size(30.dp),
                )
            }
        },
        onClick = onCLick
    )
    data class Theme(val onCLick: () -> Unit) : MenuItem(
        label = "Theme",
        icon = { Icon(imageVector = Icons.Outlined.FormatPaint, contentDescription = "Theme") },
        onClick = onCLick
    )
}