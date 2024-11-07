package com.klavs.bindle

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.klavs.bindle.ui.theme.BindleTheme
import com.klavs.bindle.data.entity.BottomNavItem
import com.klavs.bindle.ui.theme.defaultTextFont
import com.klavs.bindle.uix.view.Events
import com.klavs.bindle.uix.view.Home
import com.klavs.bindle.uix.view.auth.LogIn
import com.klavs.bindle.uix.view.map.Map
import com.klavs.bindle.uix.view.menu.Menu
import com.klavs.bindle.uix.view.communities.Communities
import com.klavs.bindle.uix.view.auth.CreateAccount
import com.klavs.bindle.uix.view.auth.CreateUserPhaseTwo
import com.klavs.bindle.uix.view.auth.CreateUserSetPasword
import com.klavs.bindle.uix.view.communities.communityPage.CommunityPage
import com.klavs.bindle.uix.view.communities.CreateCommunity
import com.klavs.bindle.uix.view.communities.communityPage.CreatePost
import com.klavs.bindle.uix.view.map.CreateEvent
import com.klavs.bindle.uix.view.menu.AppSettings
import com.klavs.bindle.uix.view.menu.Profile
import com.klavs.bindle.uix.view.menu.ResetEmail
import com.klavs.bindle.uix.view.menu.ResetPassword
import com.klavs.bindle.uix.view.menu.ThemeSettings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BindleTheme {
                NavHostWithBottomNavigation()
            }
        }
    }
}

@Composable
private fun NavHostWithBottomNavigation() {
    var navController = rememberNavController()
    val bottomBarIsEnable = rememberSaveable {
        mutableStateOf(true)
    }
    Scaffold(
        bottomBar = {
            if (bottomBarIsEnable.value) {
                BottomNavigationBar(navController = navController)
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(BottomNavItem.Home.route) {
                Home(navController = navController)
                bottomBarIsEnable.value = true
            }
            composable(BottomNavItem.Communities.route) {
                Communities(navController = navController)
                bottomBarIsEnable.value = true
            }
            composable(BottomNavItem.Map.route) {
                Map(navController = navController)
                bottomBarIsEnable.value = true
            }
            composable(BottomNavItem.Events.route) {
                Events(navController = navController)
                bottomBarIsEnable.value = true
            }
            composable(BottomNavItem.Menu.route) {
                Menu(navController = navController)
                bottomBarIsEnable.value = true
            }
            composable("log_in") {
                LogIn(navController = navController)
                bottomBarIsEnable.value = false
            }
            composable("create_user") {
                CreateAccount(navController = navController)
            }
            composable(
                "create_user_phase_two/{profilePictureUri}/{realName}/{userName}",
                arguments = listOf(navArgument("profilePictureUri") { type = NavType.StringType },
                    navArgument("realName") { type = NavType.StringType },
                    navArgument("userName") { type = NavType.StringType })
            ) {
                CreateUserPhaseTwo(
                    navController = navController,
                    profilePictureUri = it.arguments?.getString("profilePictureUri") ?: "default",
                    realName = it.arguments?.getString("realName") ?: "",
                    userName = it.arguments?.getString("userName") ?: ""
                )
            }
            composable(
                "create_user_phase_three/{profilePictureUri}/{realName}/{userName}/{email}/{phoneNumber}/{gender}/{birthDay}",
                arguments = listOf(navArgument("profilePictureUri") { type = NavType.StringType },
                    navArgument("realName") { type = NavType.StringType },
                    navArgument("userName") { type = NavType.StringType },
                    navArgument("email") { type = NavType.StringType },
                    navArgument("phoneNumber") { type = NavType.StringType },
                    navArgument("gender") { type = NavType.StringType },
                    navArgument("birthDay") { type = NavType.LongType })
            ) {
                CreateUserSetPasword(
                    navController = navController,
                    profilePictureUri = it.arguments?.getString("profilePictureUri") ?: "default",
                    realName = it.arguments?.getString("realName") ?: "",
                    userName = it.arguments?.getString("userName")!!,
                    email = it.arguments?.getString("email")!!,
                    phoneNumber = it.arguments?.getString("phoneNumber") ?: "",
                    gender = it.arguments?.getString("gender") ?: "",
                    birthDay = it.arguments?.getLong("birthDay") ?: 0L
                )
            }
            composable("menu_profile") {
                Profile(navController = navController)
                bottomBarIsEnable.value = false
            }
            composable("reset_password") { ResetPassword(navController = navController) }
            composable("reset_email") { ResetEmail(navController = navController) }
            composable("app_settings") { AppSettings(navController = navController) }
            composable("theme") { ThemeSettings(navController = navController) }
            composable("create_community") { CreateCommunity(navController = navController) }
            composable(
                "createPost/{communityId}",
                arguments = listOf(navArgument("communityId") { type = NavType.StringType })
            ) {
                CreatePost(
                    navController = navController,
                    communityId = it.arguments?.getString("communityId")!!
                )
            }
            composable(
                "community_page/{communityId}",
                arguments = listOf(
                    navArgument("communityId") { type = NavType.StringType })
            ) {
                CommunityPage(
                    navController = navController,
                    communityId = it.arguments?.getString("communityId")!!
                )
            }
            composable("create_event/{latitude}/{longitude}",
                arguments = listOf(navArgument("latitude") { type = NavType.StringType },
                    navArgument("longitude") { type = NavType.StringType }
                )
            ) {
                CreateEvent(
                    navController = navController,
                    latitude = it.arguments?.getString("latitude")!!,
                    longitude = it.arguments?.getString("longitude")!!
                )
            }

        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Communities,
        BottomNavItem.Map,
        BottomNavItem.Events,
        BottomNavItem.Menu
    )

    NavigationBar {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route
        bottomNavItems.forEach { bottomBarItem ->
            val selected = currentRoute == bottomBarItem.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (bottomBarItem.route != "map" || !selected) {
                        navController.navigate(bottomBarItem.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                icon = if (selected) bottomBarItem.selectedIcon else bottomBarItem.unselectedIcon,
                label = {
                    Text(
                        text = bottomBarItem.label,
                        fontFamily = defaultTextFont,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                })
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

}