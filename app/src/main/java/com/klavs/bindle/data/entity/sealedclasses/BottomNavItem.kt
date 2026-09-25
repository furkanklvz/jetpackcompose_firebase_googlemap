package com.klavs.bindle.data.entity.sealedclasses


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.klavs.bindle.R

sealed class BottomNavItem(
    val route: String,
    val unselectedIcon: @Composable () -> Unit,
    val selectedIcon: @Composable () -> Unit,

    val labelResource: Int
) {
    data object Home : BottomNavItem(
        "home",
        { Icon(imageVector = Icons.Outlined.Home, contentDescription = "home",) },
        { Icon(imageVector = Icons.Rounded.Home, contentDescription = "home",) },
        R.string.home
    )

    data object Communities : BottomNavItem(
        "communities",
        { Icon(imageVector = Icons.Outlined.Groups, contentDescription = "communities",) },
        { Icon(imageVector = Icons.Rounded.Groups, contentDescription = "communities",) },
        R.string.communities
    )

    data object Map : BottomNavItem(
        "map",
        {
            Icon(
                imageVector = Icons.Outlined.Map,
                contentDescription = "map"
            )
        },
        {
            Icon(
                imageVector = Icons.Rounded.Map,
                contentDescription = "map"
            )


        },
        R.string.map
    )

    data object Events : BottomNavItem(
        "events",
        {
            Icon(
                imageVector = Icons.Outlined.Celebration,
                contentDescription = "event"
            )
        },
        {
            Icon(
                imageVector = Icons.Rounded.Celebration,
                contentDescription = "event"
            )
        },
        R.string.my_events
    )

    data object Menu : BottomNavItem(
        "menu",
        { Icon(imageVector = Icons.Rounded.Menu, contentDescription = "menu") },
        { Icon(imageVector = Icons.Rounded.Menu, contentDescription = "menu") },
        R.string.menu
    )
}