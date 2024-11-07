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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.klavs.bindle.data.datastore.AppPref
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val theme = viewModel.selectedTheme.collectAsState(initial = AppPref.DEFAULT_THEME)
    val context = LocalContext.current
    val isDarkTheme = remember {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = theme.value) {
        when (theme.value) {
            "dark" -> {
                isDarkTheme.value = true
                (context as ComponentActivity).enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(
                        Color.Transparent.toArgb()
                    )
                )
            }

            "light" -> {
                isDarkTheme.value = false
                (context as ComponentActivity).enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.light(
                        Color.Transparent.toArgb(),
                        Color.Transparent.toArgb()
                    )
                )
            }

            "dynamic" -> {
                isDarkTheme.value = darkTheme
                (context as ComponentActivity).enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.Transparent.toArgb(),
                        Color.Transparent.toArgb()
                    )
                )
            }
        }
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDarkTheme.value) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                context
            )
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }






    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}