package com.yesser_studios.death_switch

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

class MigrationManager(private val context: Context) {
    companion object {
        private val Context.legacyDataStore by preferencesDataStore("deaths")
        val legacyDeathsKey = intPreferencesKey("deaths")
    }

    suspend fun migrateIfNeeded(jsonStorage: JsonDeathStorage): Boolean {
        val db = jsonStorage.deathsDatabase.first()
        
        if (db.version == 0) {
            val legacyDeaths = try {
                context.legacyDataStore.data.first()[legacyDeathsKey] ?: 0
            } catch (e: Exception) {
                0
            }
            
            if (legacyDeaths > 0) {
                jsonStorage.setTotalDeaths(legacyDeaths)
            }
            return true
        }
        return false
    }

    suspend fun needsMigration(jsonStorage: JsonDeathStorage): Boolean {
        return jsonStorage.deathsDatabase.first().version == 0
    }
}
