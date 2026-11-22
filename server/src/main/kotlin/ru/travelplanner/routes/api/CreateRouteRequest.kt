package ru.travelplanner.routes.api

data class CreateRouteRequest(
    val name: String,
    val points: List<RoutePoint>
)
