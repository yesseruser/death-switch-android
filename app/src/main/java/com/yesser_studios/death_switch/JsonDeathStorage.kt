package com.yesser_studios.death_switch

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class JsonDeathStorage(private val context: Context) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val fileName = "deaths_db.json"

    private val file: File
        get() = File(context.filesDir, fileName)

    private val _deathsDatabase = MutableStateFlow(DeathsDatabase())
    val deathsDatabase: Flow<DeathsDatabase> = _deathsDatabase.asStateFlow()

    private val writeMutex = Mutex()

    suspend fun load() {
        withContext(Dispatchers.IO) {
            try {
                if (file.exists()) {
                    val content = file.readText()
                    _deathsDatabase.value = json.decodeFromString<DeathsDatabase>(content)
                } else {
                    _deathsDatabase.value = DeathsDatabase(version = 0)
                }
            } catch (e: Exception) {
                Log.e("JsonDeathStorage", "Failed to load deaths DB", e)
            }
        }
    }

    private suspend fun save() {
        withContext(Dispatchers.IO) {
            try {
                val content = json.encodeToString(_deathsDatabase.value)
                file.writeText(content)
            } catch (e: Exception) {
                Log.e("JsonDeathStorage", "Failed to save deaths DB", e)
            }
        }
    }

    suspend fun incrementDeath() {
        writeMutex.withLock {
            val today = getCurrentDateString()
            val current = _deathsDatabase.value
            val existingRecord = current.records.find { it.date == today }

            val newRecords = if (existingRecord != null) {
                current.records.map { record ->
                    if (record.date == today) record.copy(count = record.count + 1)
                    else record
                }
            } else {
                current.records + DeathRecord(date = today, count = 1)
            }

            _deathsDatabase.value = current.copy(
                records = newRecords,
                totalDeaths = current.totalDeaths + 1
            )
            save()
        }
    }

    suspend fun setTotalDeaths(total: Int) {
        writeMutex.withLock {
            _deathsDatabase.value = _deathsDatabase.value.copy(totalDeaths = total, version = 1)
            save()
        }
    }

    fun getTotalDeaths(): Int = _deathsDatabase.value.totalDeaths

    fun getTodayDeaths(): Int {
        val today = getCurrentDateString()
        return _deathsDatabase.value.records.find { it.date == today }?.count ?: 0
    }

    fun getRecordsForDays(days: Int): List<DeathRecord> {
        if (days <= 0) return emptyList()
        val records = _deathsDatabase.value.records
        val today = getCurrentDateString()
        val cutoffDate = subtractDays(today, days - 1)

        return records
            .filter { it.date >= cutoffDate && it.date <= today }
            .sortedBy { it.date }
    }

    private fun getCurrentDateString(): String {
        val now = java.time.LocalDate.now()
        return now.toString()
    }

    private fun subtractDays(dateString: String, days: Int): String {
        val date = java.time.LocalDate.parse(dateString)
        return date.minusDays(days.toLong()).toString()
    }
}
