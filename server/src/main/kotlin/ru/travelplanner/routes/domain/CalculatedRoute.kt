package ru.travelplanner.routes.domain

data class RoutePoint(
    val latitude: Double,
    val longitude: Double
)

data class CalculatedRoute(
    val distanceMeters: Double,
    val durationSeconds: Double,
    val paths: List<List<RoutePoint>>
)
