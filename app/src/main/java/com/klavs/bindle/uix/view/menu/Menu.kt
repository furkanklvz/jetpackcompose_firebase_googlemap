package com.klavs.bindle.uix.view.menu

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.BottomNavItem
import com.klavs.bindle.data.entity.MenuItem
import com.klavs.bindle.ui.theme.defaultTextFont
import com.klavs.bindle.uix.viewmodel.MenuViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Menu(
    navController: NavHostController,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()


    Scaffold(topBar = { TopAppBar(title = { Text(text = BottomNavItem.Menu.label) }) }) { innerpadding ->
        val menuItems = listOf(
            MenuItem.Profile { navController.navigate("menu_profile") },
            MenuItem.AppSettings { navController.navigate("app_settings") },
            MenuItem.Auth(currentUser != null) {
                if (currentUser != null) {
                    viewModel.signOut()
                    navController.navigate(BottomNavItem.Home.route){
                        popUpTo(0){
                            inclusive = true
                        }
                    }
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
            items(menuItems) {
                if (currentUser != null || it !is MenuItem.Profile) {
                    MenuItemRow(item = it)
                }

            }
        }

    }
}

@Composable
fun MenuItemRow(item: MenuItem) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    Row(
        Modifier
            .fillMaxWidth(0.9f)
            .height(60.dp)
            .clickable { item.onClick.invoke() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxHeight()) {
            item.icon.invoke()
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = item.label,
                fontSize = 16.sp,
                fontFamily = defaultTextFont,
                color = if (item.label.equals("Log out")) Color.Red else Color.Unspecified
            )


        }
        Icon(
            painter = painterResource(id = R.drawable.rounded_keyboard_arrow_right_24),
            contentDescription = "click",
            modifier = Modifier.size(30.dp)
        )
    }
}



