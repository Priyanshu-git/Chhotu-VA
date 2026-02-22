package com.nexxlabs.chhotu.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generic wrapper around DataStore<Preferences>.
 * All persisted keys are defined here as constants for easy discovery.
 */
@Singleton
class AppDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object Keys {
        val COMMAND_HISTORY = stringPreferencesKey("command_history")

    }

    fun getStringFlow(key: Preferences.Key<String>, default: String = ""): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[key] ?: default
        }
    }

    suspend fun putString(key: Preferences.Key<String>, value: String) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun remove(key: Preferences.Key<String>) {
        dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
}
