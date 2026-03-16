package com.yesser_studios.death_switch

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

enum class TimeRange(val days: Int, val label: String) {
    WEEK(7, "Week"),
    MONTH(30, "Month"),
    YEAR(365, "Year")
}

class StatsRepository(private val jsonStorage: JsonDeathStorage) {

    val database: Flow<DeathsDatabase> = jsonStorage.deathsDatabase

    fun getTotalDeaths(): Int = jsonStorage.getTotalDeaths()

    fun getTodayDeaths(): Int = jsonStorage.getTodayDeaths()

    fun getRecordsForRange(range: TimeRange): List<DeathRecord> {
        return jsonStorage.getRecordsForDays(range.days)
    }

    fun getRecordsForRangeWithZeros(range: TimeRange): List<DeathRecord> {
        val records = getRecordsForRange(range)
        
        if (range == TimeRange.YEAR) {
            return aggregateToMonths(records)
        }

        val today = LocalDate.now()
        val result = mutableListOf<DeathRecord>()

        for (i in range.days - 1 downTo 0) {
            val date = today.minusDays(i.toLong())
            val dateStr = date.toString()
            val existing = records.find { it.date == dateStr }
            result.add(existing ?: DeathRecord(dateStr, 0))
        }

        return result
    }

    private fun aggregateToMonths(records: List<DeathRecord>): List<DeathRecord> {
        val recordsByMonth = if (records.isEmpty()) {
            emptyMap()
        } else {
            records
                .sortedBy { it.date }
                .groupBy { record ->
                    val date = LocalDate.parse(record.date)
                    "${date.year}-${date.monthValue.toString().padStart(2, '0')}"
                }
        }

        val today = LocalDate.now()
        val result = mutableListOf<DeathRecord>()
        
        val startDate = today.minusMonths(11)
        var currentDate = startDate

        while (!currentDate.isAfter(today)) {
            val monthKey = "${currentDate.year}-${currentDate.monthValue.toString().padStart(2, '0')}"
            
            val monthRecords = recordsByMonth[monthKey] ?: emptyList()
            val totalCount = monthRecords.sumOf { it.count }
            
            result.add(DeathRecord(monthKey, totalCount))
            
            currentDate = currentDate.plusMonths(1)
        }

        return result
    }

    suspend fun incrementDeath() {
        jsonStorage.incrementDeath()
    }
}
