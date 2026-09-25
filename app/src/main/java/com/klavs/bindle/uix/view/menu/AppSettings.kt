package com.klavs.bindle.uix.view.menu

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.sealedclasses.MenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettings(navController: NavHostController) {
    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "back"
                    )
                }
            },
            title = { Text(text = stringResource(R.string.app_settings)) })
    }) { innerPadding ->
        val menuItems = listOf(
            MenuItem.Theme{
                navController.navigate("theme")
            },
            MenuItem.Language{
                navController.navigate("language")
            }
        )
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            items(menuItems) {
                    MenuItemRow(menuItem = it)
            }
        }
    }
}


@Preview
@Composable
private fun AppSettingsPreview() {
    AppSettings(navController = rememberNavController())
}