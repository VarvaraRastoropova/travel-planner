package ru.travelplanner.locations.domain

data class FoundLocation(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val bounds: LocationBounds? = null
)
