package com.klavs.bindle.uix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klavs.bindle.data.datastore.AppPref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(private val appPref: AppPref) : ViewModel() {

    fun getTheme() = appPref.getSelectedTheme()
    fun setTheme(theme: String) {
        val themeMode: String = when (theme) {
            "light" -> "light"
            "dark" -> "dark"
            else -> AppPref.DEFAULT_THEME
        }
        viewModelScope.launch(Dispatchers.IO) {
            appPref.saveSelectedTheme(selectedTheme = themeMode)
        }

    }
}