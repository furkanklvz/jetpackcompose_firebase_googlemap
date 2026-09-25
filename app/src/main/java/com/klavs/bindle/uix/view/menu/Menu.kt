package com.klavs.bindle.uix.view.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseUser
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.sealedclasses.BottomNavItem
import com.klavs.bindle.data.entity.sealedclasses.MenuItem
import com.klavs.bindle.uix.viewmodel.MenuViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Menu(
    navController: NavHostController,
    currentUser: FirebaseUser?,
    viewModel: MenuViewModel = hiltViewModel()
) {


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(BottomNavItem.Menu.labelResource)
                    )
                }
            )
        }
    ) { innerpadding ->
        val menuItems = listOf(
            MenuItem.Account { navController.navigate("menu_profile") },
            MenuItem.AppSettings { navController.navigate("app_settings") },
            MenuItem.SupportAndFeedback { navController.navigate("support_and_feedback") },
            MenuItem.Auth(currentUser != null) {
                if (currentUser != null) {
                    viewModel.signOut(
                        uid = currentUser.uid
                    )
                } else {
                    navController.navigate("log_in")
                }
            }

        )
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerpadding.calculateTopPadding())
        ) {
            items(menuItems) { menuItem ->
                if (currentUser != null || menuItem !is MenuItem.Account) {
                    MenuItemRow(
                        menuItem = menuItem,
                        user = if (menuItem is MenuItem.Account) currentUser else null
                    )
                }

            }
        }

    }
}

@Composable
fun MenuItemRow(menuItem: MenuItem, user: FirebaseUser? = null) {
    ListItem(
        modifier = Modifier.clickable { menuItem.onClick.invoke() },
        leadingContent = {
            if (menuItem is MenuItem.Account) {
                BadgedBox(
                    badge = {
                        if (user != null) {
                            if (!user.isEmailVerified) {
                                Badge()
                            }
                        }
                    }
                ) {
                    menuItem.icon()
                }
            } else {
                menuItem.icon()
            }
        },
        headlineContent = {
            Text(
                text = stringResource(menuItem.labelResource),
                color = if (stringResource(menuItem.labelResource) == stringResource(R.string.sign_out)) MaterialTheme.colorScheme.error
                else Color.Unspecified
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                contentDescription = "click"
            )
        },
        supportingContent = menuItem.supportingContent
    )
}


@Preview
@Composable
fun MenuItemRowPreview() {
}
