package com.klavs.bindle.uix.view.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DynamicForm
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.klavs.bindle.data.datastore.AppPref
import com.klavs.bindle.uix.viewmodel.ThemeSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettings(
    navController: NavHostController,
    viewModel: ThemeSettingsViewModel = hiltViewModel()
) {
    val darkSelected = remember { mutableStateOf(false) }
    val lightSelected = remember { mutableStateOf(false) }
    val systemSelected = remember { mutableStateOf(false) }

    val currentTheme = viewModel.getTheme().collectAsState(initial = AppPref.DEFAULT_THEME)
    LaunchedEffect(key1 = currentTheme.value) {
        when (currentTheme.value) {
            "dark" -> {
                darkSelected.value = true
                lightSelected.value = false
                systemSelected.value = false
            }

            "light" -> {
                lightSelected.value = true
                darkSelected.value = false
                systemSelected.value = false
            }

            "dynamic" -> {
                systemSelected.value = true
                darkSelected.value = false
                lightSelected.value = false
            }
        }
    }

    Scaffold(
        topBar = {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "back"
                    )
                }
            },
            title = { Text(text = "Theme") })
    }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ThemeOptionRadioButton(
                    label = "System Theme",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = "dynamic"
                        )
                    },
                    selected = systemSelected.value
                ) {
                    viewModel.setTheme(theme = "dynamic")
                }
                ThemeOptionRadioButton(
                    label = "Dark",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.DarkMode,
                            contentDescription = "dark"
                        )
                    },
                    selected = darkSelected.value
                ) {
                    viewModel.setTheme(theme = "dark")
                }
                ThemeOptionRadioButton(
                    label = "Light",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.LightMode,
                            contentDescription = "light"
                        )
                    },
                    selected = lightSelected.value
                ) {
                    viewModel.setTheme(theme = "light")
                }

            }

        }
    }
}

@Composable
private fun ThemeOptionRadioButton(
    label: String,
    icon: @Composable (() -> Unit),
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(0.9f).clickable { onClick.invoke() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon.invoke()
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label)
        }

        RadioButton(selected = selected, onClick = onClick)
    }

}

@Preview
@Composable
private fun ThemePreview() {
    ThemeSettings(navController = rememberNavController())
}