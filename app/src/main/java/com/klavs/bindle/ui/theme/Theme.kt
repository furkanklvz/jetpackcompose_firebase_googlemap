package com.klavs.bindle.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.klavs.bindle.data.datastore.AppPref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@HiltViewModel
class ThemeViewModel @Inject constructor(private val appPref: AppPref) : ViewModel() {

    val selectedTheme = appPref.getSelectedTheme()
    val initialTheme = runBlocking { appPref.getSelectedTheme().first() }
}

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BindleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    viewModel: ThemeViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val initialTheme = viewModel.initialTheme
    var theme by remember { mutableStateOf(initialTheme) }
    val context = LocalContext.current
    LaunchedEffect(viewModel.selectedTheme) {
        viewModel.selectedTheme.collect { newTheme ->
            theme = newTheme
            when (newTheme) {
                "dark" -> {
                    (context as? ComponentActivity)?.enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.dark(
                            Color.Transparent.toArgb()
                        )
                    )
                }
                "light" -> {
                    (context as? ComponentActivity)?.enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.light(
                            Color.Transparent.toArgb(),
                            Color.Transparent.toArgb()
                        )
                    )
                }
                "dynamic" -> {
                    (context as? ComponentActivity)?.enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            Color.Transparent.toArgb(),
                            Color.Transparent.toArgb()
                        )
                    )
                }
            }
        }
    }

    val isDarkTheme = when (theme) {
        "dark" -> true
        "light" -> false
        "dynamic" -> darkTheme
        else -> darkTheme
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}