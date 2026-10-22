package ru.travelplanner.routes.domain

data class Preferences(
    val city: String?,
    val locationQuery: String?,
    val durationMinutes: Int,
    val maxDistanceKm: Double,
    val interests: List<String>
)
