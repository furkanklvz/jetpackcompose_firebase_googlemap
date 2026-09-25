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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.klavs.bindle.R
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
    ThemeContent(
        onBackClick = { navController.popBackStack() },
        systemSelected = systemSelected.value,
        darkSelected = darkSelected.value,
        lightSelected = lightSelected.value,
        setTheme = { viewModel.setTheme(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeContent(
    onBackClick: () -> Unit,
    systemSelected: Boolean,
    darkSelected: Boolean,
    lightSelected: Boolean,
    setTheme: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                },
                title = { Text(text = stringResource(R.string.theme)) })
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
                ThemeOptionRow(
                    label = stringResource(R.string.system_default),
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.BrightnessMedium,
                            contentDescription = "dynamic"
                        )
                    },
                    selected = systemSelected
                ) {
                    setTheme("dynamic")
                }
                ThemeOptionRow(
                    label = stringResource(R.string.dark),
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.DarkMode,
                            contentDescription = "dark"
                        )
                    },
                    selected = darkSelected
                ) {
                    setTheme("dark")
                }
                ThemeOptionRow(
                    label = stringResource(R.string.light),
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.LightMode,
                            contentDescription = "light"
                        )
                    },
                    selected = lightSelected
                ) {
                    setTheme("light")
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeOptionRow(
    label: String,
    icon: @Composable (() -> Unit),
    selected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(text = label) },
        leadingContent = icon,
        trailingContent = {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = label,
                    tint = ButtonDefaults.textButtonColors().contentColor,
                    modifier = Modifier.size(IconButtonDefaults.largeIconSize)
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )

}

@Preview
@Composable
private fun ThemePreview() {
    ThemeContent(
        onBackClick = {},
        systemSelected = true,
        darkSelected = false,
        lightSelected = false
    ) { }
}