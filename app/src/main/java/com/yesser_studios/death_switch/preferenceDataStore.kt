package com.yesser_studios.death_switch

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class preferenceDataStore(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("deaths")
        val deathsKey = intPreferencesKey("deaths")
    }

    suspend fun setDeaths(deathCount: Int) {
        context.dataStore.edit {
            preferences -> preferences[deathsKey] = deathCount
        }
    }

    fun getDeaths(): Flow<Int?> = context.dataStore.data.map {
            preferences -> preferences[deathsKey] ?: 0
    }
}