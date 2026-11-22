package ru.travelplanner.routes.domain

data class CalculatedRoute(
    val distanceMeters: Double,
    val durationSeconds: Double,
    val paths: List<List<RoutePoint>>
)
