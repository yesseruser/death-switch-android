package com.yesser_studios.death_switch

import kotlinx.serialization.Serializable

@Serializable
data class DeathRecord(
    val date: String,
    val count: Int
)

@Serializable
data class DeathsDatabase(
    val records: List<DeathRecord> = emptyList(),
    val totalDeaths: Int = 0,
    val version: Int = 1
)
