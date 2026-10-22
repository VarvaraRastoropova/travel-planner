package ru.travelplanner.trips.domain

import java.time.Instant
import java.util.UUID

data class Trip(
    val id: UUID,
    val name: String,
    val version: Long,
    val createdAt: Instant
)
