package com.klavs.bindle.data.entity


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.klavs.bindle.R

sealed class BottomNavItem(
    val route: String,
    val unselectedIcon: @Composable () -> Unit,
    val selectedIcon: @Composable () -> Unit,

    val label: String
) {
    object Home : BottomNavItem(
        "home",
        { Icon(painter = painterResource(id = R.drawable.rounded_home_24), contentDescription = "home",) },
        { Icon(painter = painterResource(id = R.drawable.filled_home), contentDescription = "home",) },
        "Home"
    )

    object Communities : BottomNavItem(
        "communities",
        { Icon(imageVector = Icons.Outlined.Groups, contentDescription = "communities",) },
        { Icon(imageVector = Icons.Filled.Groups, contentDescription = "communities",) },
        "Communities"
    )

    object Map : BottomNavItem(
        "map",
        {
            Icon(
                painter = painterResource(id = R.drawable.rounded_map_24),
                contentDescription = "map"
            )
        },
        {
            Icon(
                painter = painterResource(id = R.drawable.filled_map),
                contentDescription = "map"
            )


        },
        "Map"
    )

    object Events : BottomNavItem(
        "events",
        {
            Icon(
                painter = painterResource(id = R.drawable.rounded_celebration_24),
                contentDescription = "event"
            )
        },
        {
            Icon(
                painter = painterResource(id = R.drawable.filled_celebration),
                contentDescription = "event"
            )
        },
        "My Events"
    )

    object Menu : BottomNavItem(
        "menu",
        { Icon(painter = painterResource(id = R.drawable.rounded_menu_24), contentDescription = "menu") },
        { Icon(painter = painterResource(id = R.drawable.filled_menu), contentDescription = "menu") },
        "Menu"
    )
}