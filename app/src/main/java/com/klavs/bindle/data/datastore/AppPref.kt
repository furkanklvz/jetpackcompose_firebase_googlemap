package com.klavs.bindle.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
        const val DEFAULT_THEME = "dynamic"
    }

    suspend fun saveSelectedTheme(selectedTheme: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = selectedTheme
        }
    }

    fun getSelectedTheme(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[THEME_KEY] ?: DEFAULT_THEME
        }
    }


    private fun pinnedCommunitiesKey(userId: String) = stringSetPreferencesKey("pinned_communities_${userId}")
    suspend fun savePinnedCommunity(uid:String, communityId: String){
        dataStore.edit { preferences->
        val currentPinnedCommunities = preferences[pinnedCommunitiesKey(uid)] ?: emptySet()
            preferences[pinnedCommunitiesKey(uid)] = currentPinnedCommunities + communityId
        }
    }
    suspend fun removePinnedCommunity(userId: String, communityId: String) {
        dataStore.edit { preferences ->
            val currentPinned = preferences[pinnedCommunitiesKey(userId)] ?: emptySet()
            preferences[pinnedCommunitiesKey(userId)] = currentPinned - communityId
        }
    }
    fun getPinnedCommunities(userId: String?): Flow<List<String>> {
        return dataStore.data
            .map { preferences ->
                preferences[pinnedCommunitiesKey(userId?:"")]?.toList() ?: emptyList()
            }
    }
}