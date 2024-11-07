package com.klavs.bindle.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppPref @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        const val DEFAULT_THEME = "dynamic" // Varsayılan tema "dynamic" olabilir.
    }

    // Tema tercihini kaydetme
    suspend fun saveSelectedTheme(selectedTheme: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = selectedTheme
        }
    }

    // Tema tercihini almak için Flow kullanma
    fun getSelectedTheme(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[THEME_KEY] ?: DEFAULT_THEME // Varsayılan olarak dynamic döndür
        }
    }
}