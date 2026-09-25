package com.klavs.bindle.uix.view.menu

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.klavs.bindle.MainActivity
import com.klavs.bindle.R
import com.klavs.bindle.data.datastore.AppPref
import com.klavs.bindle.data.entity.sealedclasses.Language
import com.klavs.bindle.uix.viewmodel.ThemeSettingsViewModel
import java.util.Locale

@Composable
fun LanguageSettings(
    navController: NavHostController
) {
    val context = LocalContext.current


    val languages = listOf(
        Language.English(Locale.getDefault().language == Language.English().code),
        Language.Turkish(Locale.getDefault().language == Language.Turkish().code)
    )

    LanguagesContent(
        onBackClick = { navController.popBackStack() },
        languages = languages,
        onLanguageClick = {
            val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
            context.startActivity(intent)
        }
    )

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagesContent(
    onBackClick: () -> Unit,
    languages: List<Language>,
    onLanguageClick: (Language) -> Unit
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
                title = { Text(text = stringResource(R.string.language)) })
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
                languages.forEach { language ->
                    LanguageOptionRow(
                        language = language,
                        selected = language.selected,
                        onClick = { onLanguageClick(language) }
                    )
                }

            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LanguageOptionRow(
    language: Language,
    selected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = {
            Text(text = stringResource(language.nameResource))
        },
        leadingContent = {
            Image(
                painter = painterResource(language.imageResource),
                contentScale = ContentScale.Crop,
                contentDescription = stringResource(language.nameResource),
                modifier = Modifier.size(IconButtonDefaults.smallContainerSize())
            )
        },
        trailingContent = {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = ButtonDefaults.textButtonColors().contentColor,
                    modifier = Modifier.size(IconButtonDefaults.largeIconSize)
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Preview(locale = "tr")
@Composable
private fun ThemePreview() {
    LanguagesContent(
        onBackClick = {},
        languages = listOf(Language.English(), Language.Turkish(true)),
        onLanguageClick = {}
    )
}